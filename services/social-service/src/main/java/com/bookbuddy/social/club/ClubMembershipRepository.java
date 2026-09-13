package com.bookbuddy.social.club;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ClubMembershipRepository extends MongoRepository<ClubMembership, String> {

    boolean existsByClubIdAndUserId(String clubId, String userId);

    List<ClubMembership> findByUserId(String userId);
}
