package com.bookbuddy.catalog.domain;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface WishlistRepository extends MongoRepository<Wishlist, String> {

    Optional<Wishlist> findByUserIdAndBookId(String userId, String bookId);

    boolean existsByUserIdAndBookId(String userId, String bookId);

    List<Wishlist> findByUserId(String userId);
}
