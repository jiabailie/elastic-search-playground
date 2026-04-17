package com.muyao.elasticsearchplayground;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Highlight;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class BookIndexService implements BookOperations {
    public static final String INDEX = "books";

    private final ElasticsearchClient client;
    private final ObjectMapper objectMapper;

    public BookIndexService(ElasticsearchClient client) {
        this.client = client;
        this.objectMapper = new ObjectMapper();
    }

    public void createIndexIfMissing() throws Exception {
        boolean exists = client.indices().exists(request -> request.index(INDEX)).value();
        if (exists) {
            System.out.println("Index already exists: " + INDEX);
            return;
        }

        client.indices().create(request -> request
                .index(INDEX)
                .mappings(mapping -> mapping
                        .properties("title", Property.of(property -> property.text(text -> text)))
                        .properties("author", Property.of(property -> property.keyword(keyword -> keyword)))
                        .properties("category", Property.of(property -> property.keyword(keyword -> keyword)))
                        .properties("tags", Property.of(property -> property.keyword(keyword -> keyword)))
                        .properties("price", Property.of(property -> property.double_(number -> number)))
                        .properties("publishedYear", Property.of(property -> property.integer(number -> number)))
                        .properties("description", Property.of(property -> property.text(text -> text)))
                )
        );

        System.out.println("Created index: " + INDEX);
    }

    public void seedBooks() throws Exception {
        List<Book> books = loadBooks();
        BulkRequest.Builder builder = new BulkRequest.Builder();
        for (Book book : books) {
            builder.operations(operation -> operation.index(index -> index
                    .index(INDEX)
                    .id(book.getId())
                    .document(book)
            ));
        }

        client.bulk(builder.build());
        client.indices().refresh(request -> request.index(INDEX));
        System.out.println("Seeded " + books.size() + " books into index " + INDEX);
    }

    public void searchBooks(String keyword) throws Exception {
        Query query = Query.of(q -> q.multiMatch(m -> m
                .query(keyword)
                .fields("title", "description", "tags")
        ));

        SearchResponse<Book> response = client.search(request -> request
                .index(INDEX)
                .query(query)
                .sort(sort -> sort.field(field -> field.field("publishedYear").order(SortOrder.Desc)))
        , Book.class);

        System.out.println("Search results for keyword: " + keyword);
        response.hits().hits().forEach(hit -> {
            Book book = hit.source();
            if (book != null) {
                System.out.println("- " + book.getTitle() + " | " + book.getAuthor() + " | " + book.getPublishedYear());
            }
        });
    }

    public void aggregateTags() throws Exception {
        SearchResponse<Void> response = client.search(request -> request
                .index(INDEX)
                .size(0)
                .aggregations("popular_tags", aggregation -> aggregation
                        .terms(terms -> terms.field("tags").size(10))
                )
        , Void.class);

        System.out.println("Popular tags:");
        List<StringTermsBucket> buckets = response.aggregations()
                .get("popular_tags")
                .sterms()
                .buckets()
                .array();
        for (StringTermsBucket bucket : buckets) {
            System.out.println("- " + bucket.key().stringValue() + ": " + bucket.docCount());
        }
    }

    @Override
    public void filterByCategory(String category) throws Exception {
        SearchResponse<Book> response = client.search(request -> request
                .index(INDEX)
                .query(q -> q.term(t -> t.field("category").value(FieldValue.of(category))))
        , Book.class);

        System.out.println("Books in category: " + category);
        printBooks(response);
    }

    @Override
    public void sortByPrice(String order) throws Exception {
        SortOrder sortOrder = "desc".equalsIgnoreCase(order) ? SortOrder.Desc : SortOrder.Asc;
        SearchResponse<Book> response = client.search(request -> request
                .index(INDEX)
                .sort(sort -> sort.field(field -> field.field("price").order(sortOrder)))
        , Book.class);

        System.out.println("Books sorted by price: " + sortOrder.jsonValue());
        printBooks(response);
    }

    @Override
    public void rangeQueryByYear(int fromYear, int toYear) throws Exception {
        SearchResponse<Book> response = client.search(request -> request
                .index(INDEX)
                .query(q -> q.range(r -> r.number(n -> n
                        .field("publishedYear")
                        .gte((double) fromYear)
                        .lte((double) toYear)
                )))
                .sort(sort -> sort.field(field -> field.field("publishedYear").order(SortOrder.Asc)))
        , Book.class);

        System.out.println("Books published between " + fromYear + " and " + toYear);
        printBooks(response);
    }

    @Override
    public void highlightDescription(String keyword) throws Exception {
        Highlight highlighter = Highlight.of(h -> h.fields("description", field -> field));
        SearchResponse<Book> response = client.search(request -> request
                .index(INDEX)
                .query(q -> q.match(m -> m.field("description").query(keyword)))
                .highlight(highlighter)
        , Book.class);

        System.out.println("Highlight results for keyword: " + keyword);
        response.hits().hits().forEach(hit -> {
            Book book = hit.source();
            if (book != null) {
                System.out.println("- " + book.getTitle());
                Map<String, List<String>> highlights = hit.highlight();
                if (highlights != null && highlights.containsKey("description")) {
                    highlights.get("description").forEach(line -> System.out.println("  " + line));
                }
            }
        });
    }

    private void printBooks(SearchResponse<Book> response) {
        response.hits().hits().forEach(hit -> {
            Book book = hit.source();
            if (book != null) {
                System.out.println("- " + book.getTitle() + " | " + book.getAuthor() + " | " + book.getPublishedYear() + " | $" + book.getPrice());
            }
        });
    }

    private List<Book> loadBooks() throws Exception {
        try (InputStream inputStream = getClass().getResourceAsStream("/books.json")) {
            if (inputStream == null) {
                throw new IllegalStateException("books.json resource not found");
            }
            return objectMapper.readValue(inputStream, new TypeReference<List<Book>>() {});
        }
    }
}
