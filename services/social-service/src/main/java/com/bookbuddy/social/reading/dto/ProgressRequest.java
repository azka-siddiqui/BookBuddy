package com.bookbuddy.social.reading.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Request to add or update reading progress for a book.
 *
 * @param bookId      the book being tracked
 * @param status      target status (WISHLIST, IN_PROGRESS, FINISHED)
 * @param pageReached current page (0 if not started)
 */
public record ProgressRequest(
        @NotBlank String bookId,
        @NotBlank String status,
        @Min(0) int pageReached
) {
}
