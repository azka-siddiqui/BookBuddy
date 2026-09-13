package com.bookbuddy.social.club.dto;

import com.bookbuddy.social.club.BookClub;

import java.util.List;

/**
 * A recommended club plus a human-readable reason, matching the recommendations
 * page copy ("Based on your interest in War, Coming of Age, we think this club is
 * a great fit").
 */
public record ClubRecommendation(
        String id,
        String name,
        List<String> genres,
        String reason
) {
    public static ClubRecommendation from(BookClub club) {
        String reason = "Based on your interest in "
                + String.join(", ", club.getGenres())
                + ", we think this club is a great fit";
        return new ClubRecommendation(club.getId(), club.getName(), club.getGenres(), reason);
    }
}
