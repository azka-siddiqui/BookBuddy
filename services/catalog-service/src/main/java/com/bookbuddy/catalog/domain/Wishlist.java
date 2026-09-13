package com.bookbuddy.catalog.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * A single (user, book) wishlist entry. The compound unique index guarantees a
 * user cannot wishlist the same book twice.
 */
@Document(collection = "wishlists")
@CompoundIndex(name = "uk_user_book", def = "{'userId': 1, 'bookId': 1}", unique = true)
public class Wishlist {

    @Id
    private String id;
    private String userId;
    private String bookId;
    private Instant createdAt;

    public Wishlist() {
    }

    public Wishlist(String id, String userId, String bookId, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.bookId = bookId;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
