package com.bookbuddy.social.reading.dto;

/**
 * Completion-rate metric for a book (R13): of the readers who started it, how
 * many finished.
 *
 * @param bookId   the book
 * @param started  number of readers who started (IN_PROGRESS or FINISHED)
 * @param finished number of readers who finished
 * @param rate     finished / started, in the range 0..1 (0 when nobody started)
 */
public record CompletionRate(String bookId, long started, long finished, double rate) {
}
