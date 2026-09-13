package com.bookbuddy.social.book;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface BookViewRepository extends MongoRepository<BookView, String> {

    List<BookView> findByTagsIn(List<String> tags);
}
