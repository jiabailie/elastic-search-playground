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
}
