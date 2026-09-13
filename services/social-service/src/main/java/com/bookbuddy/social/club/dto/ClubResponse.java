package com.bookbuddy.social.club.dto;

import com.bookbuddy.social.club.BookClub;

import java.util.List;

/**
 * Public representation of a book club.
 */
public record ClubResponse(
        String id,
        String name,
        List<String> genres,
        int capacity,
        int memberCount,
        boolean full
) {
    public static ClubResponse from(BookClub club) {
        return new ClubResponse(
                club.getId(),
                club.getName(),
                club.getGenres(),
                club.getCapacity(),
                club.getMemberCount(),
                club.isFull());
    }
}
