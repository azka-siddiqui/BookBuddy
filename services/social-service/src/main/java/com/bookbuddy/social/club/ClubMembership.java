package com.bookbuddy.social.club;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Records that a user has joined a club. The unique compound index prevents a user
 * from joining the same club twice.
 */
@Document(collection = "club_memberships")
@CompoundIndex(name = "uk_club_user", def = "{'clubId': 1, 'userId': 1}", unique = true)
public class ClubMembership {

    @Id
    private String id;
    private String clubId;
    private String userId;
    private Instant joinedAt;

    public ClubMembership() {
    }

    public ClubMembership(String id, String clubId, String userId, Instant joinedAt) {
        this.id = id;
        this.clubId = clubId;
        this.userId = userId;
        this.joinedAt = joinedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClubId() {
        return clubId;
    }

    public void setClubId(String clubId) {
        this.clubId = clubId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(Instant joinedAt) {
        this.joinedAt = joinedAt;
    }
}
