package com.bookbuddy.catalog.service;

import com.bookbuddy.catalog.domain.Book;
import com.bookbuddy.catalog.domain.BookRepository;
import com.bookbuddy.catalog.error.NotFoundException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Read-side operations over the book catalog: title search (R6) and the
 * top-rated (R9) / most-wishlisted (R10) rankings shown on the home page.
 */
@Service
public class BookService {

    /** Upper bound on how many "top" books a caller may request. */
    private static final int MAX_TOP = 50;

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public Book getById(String id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Book not found: " + id));
    }

    /**
     * Searches books by (partial, case-insensitive) title. (R6)
     *
     * @param query title fragment
     * @param limit maximum results
     */
    public List<Book> searchByTitle(String query, int limit) {
        Pageable page = PageRequest.of(0, clampLimit(limit));
        return bookRepository.findByTitleContainingIgnoreCase(query.trim(), page);
    }

    /** Returns the top-N books by average rating. (R9) */
    public List<Book> topByRating(int limit) {
        return bookRepository.findByOrderByRatingAvgDesc(PageRequest.of(0, clampTop(limit)));
    }

    /** Returns the top-N books by wishlist count. (R10) */
    public List<Book> topByWishlistCount(int limit) {
        return bookRepository.findByOrderByWishlistCountDesc(PageRequest.of(0, clampTop(limit)));
    }

    private int clampLimit(int limit) {
        if (limit <= 0) {
            return 20;
        }
        return Math.min(limit, 100);
    }

    private int clampTop(int limit) {
        if (limit <= 0) {
            return 5;
        }
        return Math.min(limit, MAX_TOP);
    }
}
