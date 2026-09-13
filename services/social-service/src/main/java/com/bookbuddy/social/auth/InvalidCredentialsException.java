package com.bookbuddy.social.auth;

/**
 * Thrown when authentication fails. The message is intentionally generic so it
 * does not reveal whether the username or the password was incorrect.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Invalid username or password");
    }
}
