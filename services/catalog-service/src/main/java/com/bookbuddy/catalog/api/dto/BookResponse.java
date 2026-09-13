package com.bookbuddy.catalog.api.dto;

import com.bookbuddy.catalog.domain.Book;

import java.util.List;

/**
 * Public representation of a book returned by the catalog API.
 */
public record BookResponse(
        String id,
        String openLibraryId,
        String title,
        List<String> authors,
        int pageCount,
        List<String> tags,
        String coverUrl,
        String description,
        double ratingAvg,
        int ratingCount,
        int wishlistCount
) {
    public static BookResponse from(Book book) {
        return new BookResponse(
                book.getId(),
                book.getOpenLibraryId(),
                book.getTitle(),
                book.getAuthors(),
                book.getPageCount(),
                book.getTags(),
                book.getCoverUrl(),
                book.getDescription(),
                book.getRatingAvg(),
                book.getRatingCount(),
                book.getWishlistCount()
        );
    }
}
