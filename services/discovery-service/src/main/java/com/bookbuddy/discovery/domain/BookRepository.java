package com.bookbuddy.discovery.domain;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface BookRepository extends MongoRepository<Book, String> {

    Optional<Book> findByOpenLibraryId(String openLibraryId);

    boolean existsByOpenLibraryId(String openLibraryId);
}
