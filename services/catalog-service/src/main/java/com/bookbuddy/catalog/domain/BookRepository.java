package com.bookbuddy.catalog.domain;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface BookRepository extends MongoRepository<Book, String> {

    /**
     * Case-insensitive title search (R6). Uses a regex match so partial titles
     * (e.g. "harry potter") match works whose titles contain the term.
     */
    List<Book> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    /** Top books by average rating (R9). */
    List<Book> findByOrderByRatingAvgDesc(Pageable pageable);

    /** Top books by wishlist count (R10). */
    List<Book> findByOrderByWishlistCountDesc(Pageable pageable);
}
