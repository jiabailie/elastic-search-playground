package com.muyao.elasticsearchplayground;

public interface BookOperations {
    void createIndexIfMissing() throws Exception;

    void seedBooks() throws Exception;

    void searchBooks(String keyword) throws Exception;

    void aggregateTags() throws Exception;

    void filterByCategory(String category) throws Exception;

    void sortByPrice(String order) throws Exception;

    void rangeQueryByYear(int fromYear, int toYear) throws Exception;

    void highlightDescription(String keyword) throws Exception;

    void searchBool(String keyword, String category) throws Exception;

    void searchFuzzy(String keyword) throws Exception;

    void searchPage(String keyword, int from, int size) throws Exception;

    void updatePrice(String id, double price) throws Exception;

    void deleteBook(String id) throws Exception;

    void upsertBook(String filePath) throws Exception;
}
