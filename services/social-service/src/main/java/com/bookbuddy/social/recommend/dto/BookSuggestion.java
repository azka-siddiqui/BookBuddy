package com.bookbuddy.social.recommend.dto;

import com.bookbuddy.social.book.BookView;

import java.util.List;

/**
 * A recommended book, shown on the recommendations page.
 */
public record BookSuggestion(
        String id,
        String title,
        List<String> authors,
        List<String> tags,
        String coverUrl
) {
    public static BookSuggestion from(BookView book) {
        return new BookSuggestion(
                book.getId(),
                book.getTitle(),
                book.getAuthors(),
                book.getTags(),
                book.getCoverUrl());
    }
}
