package com.muyao.elasticsearchplayground;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers(disabledWithoutDocker = true)
class BookIndexServiceIT {
    @Container
    static final ElasticsearchContainer ELASTICSEARCH_CONTAINER = new ElasticsearchContainer(
            "docker.elastic.co/elasticsearch/elasticsearch:8.15.3")
            .withEnv("xpack.security.enabled", "false")
            .withEnv("discovery.type", "single-node");

    private ElasticsearchClient client;
    private BookIndexService service;

    @BeforeEach
    void setUp() throws Exception {
        client = ElasticsearchClientFactory.createClient("http://" + ELASTICSEARCH_CONTAINER.getHttpHostAddress());
        service = new BookIndexService(client);

        if (client.indices().exists(request -> request.index(BookIndexService.INDEX)).value()) {
            client.indices().delete(request -> request.index(BookIndexService.INDEX));
        }

        service.createIndexIfMissing();
        service.seedBooks();
    }

    @Test
    void bool_query_filters_by_keyword_and_category() throws Exception {
        SearchResponse<Book> response = service.executeBoolSearch("query", "search");

        assertEquals(1, response.hits().hits().size());
        assertEquals("book-3", response.hits().hits().get(0).id());
    }

    @Test
    void fuzzy_query_matches_close_title_typo() throws Exception {
        SearchResponse<Book> response = service.executeFuzzySearch("Elasticsarch");

        assertFalse(response.hits().hits().isEmpty());
        assertEquals("book-1", response.hits().hits().get(0).id());
    }

    @Test
    void paged_search_returns_requested_slice() throws Exception {
        SearchResponse<Book> response = service.executePagedSearch("search", 1, 2);

        assertEquals(2, response.hits().hits().size());
        assertEquals("book-1", response.hits().hits().get(0).id());
        assertEquals("book-2", response.hits().hits().get(1).id());
    }

    @Test
    void update_delete_and_upsert_change_index_state() throws Exception {
        service.updatePrice("book-1", 88.75);
        client.indices().refresh(request -> request.index(BookIndexService.INDEX));

        GetResponse<Book> updated = client.get(request -> request
                .index(BookIndexService.INDEX)
                .id("book-1"), Book.class);
        assertTrue(updated.found());
        assertNotNull(updated.source());
        assertEquals(88.75, updated.source().getPrice());

        service.deleteBook("book-4");
        client.indices().refresh(request -> request.index(BookIndexService.INDEX));

        GetResponse<Book> deleted = client.get(request -> request
                .index(BookIndexService.INDEX)
                .id("book-4"), Book.class);
        assertFalse(deleted.found());

        service.upsertBook(Path.of("src/main/resources/book-upsert.json").toString());
        client.indices().refresh(request -> request.index(BookIndexService.INDEX));

        GetResponse<Book> inserted = client.get(request -> request
                .index(BookIndexService.INDEX)
                .id("book-5"), Book.class);
        assertTrue(inserted.found());
        assertNotNull(inserted.source());
        assertEquals("Search Relevance Tuning", inserted.source().getTitle());
    }
}
