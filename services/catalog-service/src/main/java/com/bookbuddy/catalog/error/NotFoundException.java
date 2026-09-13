package com.bookbuddy.catalog.error;

/**
 * Thrown when a referenced resource (e.g. a book) does not exist.
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
