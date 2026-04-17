package com.muyao.elasticsearchplayground;

public class CommandDispatcher {
    private final BookOperations operations;

    public CommandDispatcher(BookOperations operations) {
        this.operations = operations;
    }

    public void dispatch(String[] args) throws Exception {
        if (args.length == 0) {
            throw new IllegalArgumentException(usage());
        }

        String command = args[0];
        switch (command) {
            case "create-index":
                operations.createIndexIfMissing();
                break;
            case "seed-books":
                operations.seedBooks();
                break;
            case "search-books":
                requireLength(args, 2, "search-books <keyword>");
                operations.searchBooks(args[1]);
                break;
            case "aggregate-tags":
                operations.aggregateTags();
                break;
            case "filter-category":
                requireLength(args, 2, "filter-category <category>");
                operations.filterByCategory(args[1]);
                break;
            case "sort-price":
                requireLength(args, 2, "sort-price <asc|desc>");
                requireSortOrder(args[1]);
                operations.sortByPrice(args[1]);
                break;
            case "range-year":
                requireLength(args, 3, "range-year <fromYear> <toYear>");
                operations.rangeQueryByYear(parseYear(args[1], "range-year <fromYear> <toYear>"),
                        parseYear(args[2], "range-year <fromYear> <toYear>"));
                break;
            case "highlight-description":
                requireLength(args, 2, "highlight-description <keyword>");
                operations.highlightDescription(args[1]);
                break;
            default:
                throw new IllegalArgumentException(usage());
        }
    }

    public String usage() {
        return String.join(System.lineSeparator(),
                "Elasticsearch Playground",
                "Commands:",
                "  create-index",
                "  seed-books",
                "  search-books <keyword>",
                "  aggregate-tags",
                "  filter-category <category>",
                "  sort-price <asc|desc>",
                "  range-year <fromYear> <toYear>",
                "  highlight-description <keyword>");
    }

    private static void requireLength(String[] args, int length, String commandUsage) {
        if (args.length < length) {
            throw new IllegalArgumentException("Usage: " + commandUsage);
        }
    }

    private static void requireSortOrder(String order) {
        if (!"asc".equalsIgnoreCase(order) && !"desc".equalsIgnoreCase(order)) {
            throw new IllegalArgumentException("Usage: sort-price <asc|desc>");
        }
    }

    private static int parseYear(String value, String commandUsage) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Usage: " + commandUsage, exception);
        }
    }
}
