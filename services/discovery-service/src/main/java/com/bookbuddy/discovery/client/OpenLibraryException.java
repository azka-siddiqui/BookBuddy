package com.bookbuddy.discovery.client;

/**
 * Raised when the Open Library source API cannot be reached or returns an
 * unexpected response.
 */
public class OpenLibraryException extends RuntimeException {

    public OpenLibraryException(String message, Throwable cause) {
        super(message, cause);
    }
}
