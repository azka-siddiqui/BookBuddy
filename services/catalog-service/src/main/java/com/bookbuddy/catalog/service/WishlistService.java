package com.bookbuddy.catalog.service;

import com.bookbuddy.catalog.domain.Book;
import com.bookbuddy.catalog.domain.Wishlist;
import com.bookbuddy.catalog.domain.WishlistRepository;
import com.bookbuddy.catalog.error.NotFoundException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Manages wishlist entries (R7) and keeps the denormalized
 * {@code books.wishlistCount} used by the "most wishlisted" ranking (R10) in sync.
 *
 * <p>The counter on the book is updated with an atomic {@code $inc} via
 * {@link MongoTemplate} so concurrent add/remove operations remain correct.
 */
@Service
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final BookService bookService;
    private final MongoTemplate mongoTemplate;

    public WishlistService(WishlistRepository wishlistRepository,
                           BookService bookService,
                           MongoTemplate mongoTemplate) {
        this.wishlistRepository = wishlistRepository;
        this.bookService = bookService;
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Adds a book to a user's wishlist. Idempotent: adding an already-wishlisted
     * book is a no-op and does not double-count.
     *
     * @return {@code true} if a new entry was created, {@code false} if it already existed
     */
    public boolean add(String userId, String bookId) {
        // Validates the book exists (throws NotFoundException otherwise).
        bookService.getById(bookId);

        if (wishlistRepository.existsByUserIdAndBookId(userId, bookId)) {
            return false;
        }
        try {
            wishlistRepository.save(new Wishlist(
                    UUID.randomUUID().toString(), userId, bookId, Instant.now()));
        } catch (DuplicateKeyException e) {
            // Lost a race with a concurrent add; treat as already present.
            return false;
        }
        adjustWishlistCount(bookId, 1);
        return true;
    }

    /**
     * Removes a book from a user's wishlist.
     *
     * @return {@code true} if an entry was removed, {@code false} if none existed
     */
    public boolean remove(String userId, String bookId) {
        return wishlistRepository.findByUserIdAndBookId(userId, bookId)
                .map(entry -> {
                    wishlistRepository.delete(entry);
                    adjustWishlistCount(bookId, -1);
                    return true;
                })
                .orElse(false);
    }

    public List<Wishlist> forUser(String userId) {
        return wishlistRepository.findByUserId(userId);
    }

    private void adjustWishlistCount(String bookId, int delta) {
        mongoTemplate.updateFirst(
                new Query(Criteria.where("_id").is(bookId)),
                new Update().inc("wishlistCount", delta),
                Book.class);
    }
}
