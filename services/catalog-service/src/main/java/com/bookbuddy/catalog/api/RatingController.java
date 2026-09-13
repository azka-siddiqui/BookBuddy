package com.bookbuddy.catalog.api;

import com.bookbuddy.catalog.service.RatingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Rating endpoint. A user rates a book 1..5; the book's aggregate rating is
 * recomputed and feeds the top-rated ranking (R9).
 */
@RestController
@RequestMapping("/api/ratings")
public class RatingController {

    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @PutMapping("/{bookId}")
    public ResponseEntity<Void> rate(@RequestHeader("X-User-Id") String userId,
                                     @PathVariable String bookId,
                                     @Valid @RequestBody RateRequest request) {
        ratingService.rate(userId, bookId, request.score());
        return ResponseEntity.noContent().build();
    }

    public record RateRequest(@Min(1) @Max(5) int score) {
    }
}
