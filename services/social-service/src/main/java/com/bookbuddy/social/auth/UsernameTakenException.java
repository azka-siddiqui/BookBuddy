package com.bookbuddy.social.auth;

/**
 * Thrown on registration when the requested username is already in use.
 */
public class UsernameTakenException extends RuntimeException {

    public UsernameTakenException(String username) {
        super("Username already taken: " + username);
    }
}
