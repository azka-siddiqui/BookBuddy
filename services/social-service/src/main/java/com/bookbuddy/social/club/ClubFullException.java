package com.bookbuddy.social.club;

/**
 * Thrown when a user attempts to join a club that has reached its capacity.
 */
public class ClubFullException extends RuntimeException {

    public ClubFullException(String clubId) {
        super("Book club is full: " + clubId);
    }
}
