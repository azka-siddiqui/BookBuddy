package com.bookbuddy.social.reading;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ReadingProgressRepository extends MongoRepository<ReadingProgress, String> {

    Optional<ReadingProgress> findByUserIdAndBookId(String userId, String bookId);

    List<ReadingProgress> findByUserId(String userId);

    List<ReadingProgress> findByUserIdAndStatus(String userId, ReadingStatus status);

    long countByUserId(String userId);

    long countByUserIdAndStatus(String userId, ReadingStatus status);

    /** All records for a book across users, used for the completion-rate metric (R13). */
    List<ReadingProgress> findByBookId(String bookId);

    long countByBookId(String bookId);

    long countByBookIdAndStatus(String bookId, ReadingStatus status);
}
