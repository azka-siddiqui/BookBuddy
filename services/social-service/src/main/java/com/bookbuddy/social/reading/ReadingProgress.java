package com.bookbuddy.social.reading;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * A user's reading record for a single book. The status transitions
 * WISHLIST -> IN_PROGRESS -> FINISHED as the user makes progress.
 */
@Document(collection = "reading_progress")
@CompoundIndex(name = "uk_user_book_progress", def = "{'userId': 1, 'bookId': 1}", unique = true)
public class ReadingProgress {

    @Id
    private String id;

    @Indexed
    private String userId;

    private String bookId;

    @Indexed
    private ReadingStatus status;

    private int pageReached;
    private int totalPages;

    private Instant startedAt;
    private Instant finishedAt;
    private Instant updatedAt;

    public ReadingProgress() {
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

    public ReadingStatus getStatus() {
        return status;
    }

    public void setStatus(ReadingStatus status) {
        this.status = status;
    }

    public int getPageReached() {
        return pageReached;
    }

    public void setPageReached(int pageReached) {
        this.pageReached = pageReached;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(Instant finishedAt) {
        this.finishedAt = finishedAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
