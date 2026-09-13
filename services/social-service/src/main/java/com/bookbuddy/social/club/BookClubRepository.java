package com.bookbuddy.social.club;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface BookClubRepository extends MongoRepository<BookClub, String> {

    List<BookClub> findByGenresIn(List<String> genres);
}
