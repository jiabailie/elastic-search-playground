package com.muyao.elasticsearchplayground;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CommandDispatcherTest {
    @Test
    void dispatches_create_index() throws Exception {
        RecordingOperations operations = new RecordingOperations();
        CommandDispatcher dispatcher = new CommandDispatcher(operations);

        dispatcher.dispatch(new String[] {"create-index"});

        assertEquals("create-index", operations.lastCommand);
    }

    @Test
    void dispatches_search_books_with_keyword() throws Exception {
        RecordingOperations operations = new RecordingOperations();
        CommandDispatcher dispatcher = new CommandDispatcher(operations);

        dispatcher.dispatch(new String[] {"search-books", "elastic"});

        assertEquals("search-books:elastic", operations.lastCommand);
    }

    @Test
    void dispatches_filter_sort_range_and_highlight() throws Exception {
        RecordingOperations operations = new RecordingOperations();
        CommandDispatcher dispatcher = new CommandDispatcher(operations);

        dispatcher.dispatch(new String[] {"filter-category", "search"});
        assertEquals("filter-category:search", operations.lastCommand);

        dispatcher.dispatch(new String[] {"sort-price", "desc"});
        assertEquals("sort-price:desc", operations.lastCommand);

        dispatcher.dispatch(new String[] {"range-year", "2021", "2023"});
        assertEquals("range-year:2021:2023", operations.lastCommand);

        dispatcher.dispatch(new String[] {"highlight-description", "query"});
        assertEquals("highlight-description:query", operations.lastCommand);
    }

    @Test
    void dispatches_bool_fuzzy_paging_update_delete_and_upsert() throws Exception {
        RecordingOperations operations = new RecordingOperations();
        CommandDispatcher dispatcher = new CommandDispatcher(operations);

        dispatcher.dispatch(new String[] {"search-bool", "elastic", "search"});
        assertEquals("search-bool:elastic:search", operations.lastCommand);

        dispatcher.dispatch(new String[] {"search-fuzzy", "elastcsearch"});
        assertEquals("search-fuzzy:elastcsearch", operations.lastCommand);

        dispatcher.dispatch(new String[] {"search-page", "search", "1", "2"});
        assertEquals("search-page:search:1:2", operations.lastCommand);

        dispatcher.dispatch(new String[] {"update-price", "book-1", "55.5"});
        assertEquals("update-price:book-1:55.5", operations.lastCommand);

        dispatcher.dispatch(new String[] {"delete-book", "book-2"});
        assertEquals("delete-book:book-2", operations.lastCommand);

        dispatcher.dispatch(new String[] {"upsert-book", "src/main/resources/book-upsert.json"});
        assertEquals("upsert-book:src/main/resources/book-upsert.json", operations.lastCommand);
    }

    @Test
    void rejects_missing_required_arguments() {
        RecordingOperations operations = new RecordingOperations();
        CommandDispatcher dispatcher = new CommandDispatcher(operations);

        assertThrows(IllegalArgumentException.class, () -> dispatcher.dispatch(new String[] {"search-books"}));
        assertThrows(IllegalArgumentException.class, () -> dispatcher.dispatch(new String[] {"filter-category"}));
        assertThrows(IllegalArgumentException.class, () -> dispatcher.dispatch(new String[] {"sort-price"}));
        assertThrows(IllegalArgumentException.class, () -> dispatcher.dispatch(new String[] {"range-year", "2021"}));
        assertThrows(IllegalArgumentException.class, () -> dispatcher.dispatch(new String[] {"highlight-description"}));
    }

    @Test
    void rejects_invalid_sort_order_and_year_range_values() {
        RecordingOperations operations = new RecordingOperations();
        CommandDispatcher dispatcher = new CommandDispatcher(operations);

        assertThrows(IllegalArgumentException.class, () -> dispatcher.dispatch(new String[] {"sort-price", "highest"}));
        assertThrows(IllegalArgumentException.class, () -> dispatcher.dispatch(new String[] {"range-year", "start", "2023"}));
        assertThrows(IllegalArgumentException.class, () -> dispatcher.dispatch(new String[] {"search-page", "search", "-1", "2"}));
        assertThrows(IllegalArgumentException.class, () -> dispatcher.dispatch(new String[] {"search-page", "search", "0", "0"}));
        assertThrows(IllegalArgumentException.class, () -> dispatcher.dispatch(new String[] {"update-price", "book-1", "free"}));
    }

    @Test
    void rejects_unknown_command() {
        RecordingOperations operations = new RecordingOperations();
        CommandDispatcher dispatcher = new CommandDispatcher(operations);

        assertThrows(IllegalArgumentException.class, () -> dispatcher.dispatch(new String[] {"unknown"}));
    }

    private static final class RecordingOperations implements BookOperations {
        private String lastCommand;

        @Override
        public void createIndexIfMissing() {
            lastCommand = "create-index";
        }

        @Override
        public void seedBooks() {
            lastCommand = "seed-books";
        }

        @Override
        public void searchBooks(String keyword) {
            lastCommand = "search-books:" + keyword;
        }

        @Override
        public void aggregateTags() {
            lastCommand = "aggregate-tags";
        }

        @Override
        public void filterByCategory(String category) {
            lastCommand = "filter-category:" + category;
        }

        @Override
        public void sortByPrice(String order) {
            lastCommand = "sort-price:" + order;
        }

        @Override
        public void rangeQueryByYear(int fromYear, int toYear) {
            lastCommand = "range-year:" + fromYear + ":" + toYear;
        }

        @Override
        public void highlightDescription(String keyword) {
            lastCommand = "highlight-description:" + keyword;
        }

        @Override
        public void searchBool(String keyword, String category) {
            lastCommand = "search-bool:" + keyword + ":" + category;
        }

        @Override
        public void searchFuzzy(String keyword) {
            lastCommand = "search-fuzzy:" + keyword;
        }

        @Override
        public void searchPage(String keyword, int from, int size) {
            lastCommand = "search-page:" + keyword + ":" + from + ":" + size;
        }

        @Override
        public void updatePrice(String id, double price) {
            lastCommand = "update-price:" + id + ":" + price;
        }

        @Override
        public void deleteBook(String id) {
            lastCommand = "delete-book:" + id;
        }

        @Override
        public void upsertBook(String filePath) {
            lastCommand = "upsert-book:" + filePath;
        }
    }
}
