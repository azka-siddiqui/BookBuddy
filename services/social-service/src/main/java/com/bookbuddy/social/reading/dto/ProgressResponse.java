package com.bookbuddy.social.reading.dto;

import com.bookbuddy.social.book.BookView;
import com.bookbuddy.social.reading.ReadingProgress;

import java.util.List;

/**
 * Reading progress enriched with book details for display on the profile page.
 */
public record ProgressResponse(
        String bookId,
        String title,
        List<String> authors,
        String status,
        int pageReached,
        int totalPages,
        String coverUrl
) {
    public static ProgressResponse from(ReadingProgress progress, BookView book) {
        return new ProgressResponse(
                progress.getBookId(),
                book == null ? null : book.getTitle(),
                book == null ? List.of() : book.getAuthors(),
                progress.getStatus() == null ? null : progress.getStatus().name(),
                progress.getPageReached(),
                progress.getTotalPages(),
                book == null ? null : book.getCoverUrl());
    }
}
