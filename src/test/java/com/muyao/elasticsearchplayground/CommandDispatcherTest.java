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
    }
}
