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
            case "search-bool":
                requireLength(args, 3, "search-bool <keyword> <category>");
                operations.searchBool(args[1], args[2]);
                break;
            case "search-fuzzy":
                requireLength(args, 2, "search-fuzzy <keyword>");
                operations.searchFuzzy(args[1]);
                break;
            case "search-page":
                requireLength(args, 4, "search-page <keyword> <from> <size>");
                operations.searchPage(args[1],
                        parseNonNegativeInt(args[2], "search-page <keyword> <from> <size>"),
                        parsePositiveInt(args[3], "search-page <keyword> <from> <size>"));
                break;
            case "update-price":
                requireLength(args, 3, "update-price <id> <price>");
                operations.updatePrice(args[1], parsePrice(args[2], "update-price <id> <price>"));
                break;
            case "delete-book":
                requireLength(args, 2, "delete-book <id>");
                operations.deleteBook(args[1]);
                break;
            case "upsert-book":
                requireLength(args, 2, "upsert-book <jsonFilePath>");
                operations.upsertBook(args[1]);
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
                "  highlight-description <keyword>",
                "  search-bool <keyword> <category>",
                "  search-fuzzy <keyword>",
                "  search-page <keyword> <from> <size>",
                "  update-price <id> <price>",
                "  delete-book <id>",
                "  upsert-book <jsonFilePath>");
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

    private static int parseNonNegativeInt(String value, String commandUsage) {
        int parsed = parseYear(value, commandUsage);
        if (parsed < 0) {
            throw new IllegalArgumentException("Usage: " + commandUsage);
        }
        return parsed;
    }

    private static int parsePositiveInt(String value, String commandUsage) {
        int parsed = parseYear(value, commandUsage);
        if (parsed <= 0) {
            throw new IllegalArgumentException("Usage: " + commandUsage);
        }
        return parsed;
    }

    private static double parsePrice(String value, String commandUsage) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Usage: " + commandUsage, exception);
        }
    }
}
