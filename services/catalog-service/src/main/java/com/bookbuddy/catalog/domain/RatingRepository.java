package com.bookbuddy.catalog.domain;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface RatingRepository extends MongoRepository<Rating, String> {

    Optional<Rating> findByUserIdAndBookId(String userId, String bookId);

    List<Rating> findByBookId(String bookId);
}
