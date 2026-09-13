package com.bookbuddy.catalog.service;

import com.bookbuddy.catalog.domain.Book;
import com.bookbuddy.catalog.domain.Rating;
import com.bookbuddy.catalog.domain.RatingRepository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Records user ratings and maintains the denormalized {@code ratingAvg} /
 * {@code ratingCount} on the book, which back the top-rated ranking (R9).
 *
 * <p>Rather than tracking a running average incrementally (which is fragile when
 * ratings are updated), the average is recomputed from all ratings for the book
 * on each write. For a demo-scale dataset this is simple and always correct.
 */
@Service
public class RatingService {

    private final RatingRepository ratingRepository;
    private final BookService bookService;
    private final MongoTemplate mongoTemplate;

    public RatingService(RatingRepository ratingRepository,
                         BookService bookService,
                         MongoTemplate mongoTemplate) {
        this.ratingRepository = ratingRepository;
        this.bookService = bookService;
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Creates or updates a user's rating for a book and refreshes the book's
     * aggregate rating fields.
     *
     * @param score rating in the range 1..5
     */
    public void rate(String userId, String bookId, int score) {
        if (score < 1 || score > 5) {
            throw new IllegalArgumentException("Rating score must be between 1 and 5");
        }
        // Validates the book exists.
        bookService.getById(bookId);

        Rating rating = ratingRepository.findByUserIdAndBookId(userId, bookId)
                .orElseGet(() -> {
                    Rating r = new Rating();
                    r.setId(UUID.randomUUID().toString());
                    r.setUserId(userId);
                    r.setBookId(bookId);
                    r.setCreatedAt(Instant.now());
                    return r;
                });
        rating.setScore(score);
        ratingRepository.save(rating);

        recomputeBookRating(bookId);
    }

    private void recomputeBookRating(String bookId) {
        List<Rating> ratings = ratingRepository.findByBookId(bookId);
        int count = ratings.size();
        double avg = count == 0
                ? 0.0
                : ratings.stream().mapToInt(Rating::getScore).average().orElse(0.0);

        mongoTemplate.updateFirst(
                new Query(Criteria.where("_id").is(bookId)),
                new Update().set("ratingAvg", round1(avg)).set("ratingCount", count),
                Book.class);
    }

    private double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
