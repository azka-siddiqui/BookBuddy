package com.bookbuddy.catalog.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * A user's rating (1..5) of a book. One rating per (user, book) enforced by a
 * compound unique index; re-rating updates the existing document.
 */
@Document(collection = "ratings")
@CompoundIndex(name = "uk_user_book_rating", def = "{'userId': 1, 'bookId': 1}", unique = true)
public class Rating {

    @Id
    private String id;
    private String userId;
    private String bookId;
    private int score;
    private Instant createdAt;

    public Rating() {
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

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
