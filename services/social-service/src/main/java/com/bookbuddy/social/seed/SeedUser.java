package com.bookbuddy.social.seed;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.List;

/**
 * Shape of a record in {@code seed/users.json}. Note the plaintext {@code password}
 * field, which is hashed by the seeder before persistence and never stored as-is.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SeedUser(
        String _id,
        String username,
        String displayName,
        String password,
        String persona,
        List<String> roles,
        int streakCount,
        Instant lastReadDate,
        Instant createdAt
) {
}
