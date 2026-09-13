package com.bookbuddy.catalog.service;

import com.bookbuddy.catalog.domain.Book;
import com.bookbuddy.catalog.domain.Rating;
import com.bookbuddy.catalog.domain.RatingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RatingServiceTest {

    @Mock
    private RatingRepository ratingRepository;
    @Mock
    private BookService bookService;
    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private RatingService ratingService;

    private Rating rating(int score) {
        Rating r = new Rating();
        r.setScore(score);
        return r;
    }

    @Test
    void rejectsScoreOutOfRange() {
        assertThatThrownBy(() -> ratingService.rate("u1", "b1", 6))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ratingService.rate("u1", "b1", 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void recomputesAverageAndCountAfterRating() {
        when(bookService.getById("b1")).thenReturn(new Book());
        when(ratingRepository.findByUserIdAndBookId("u1", "b1")).thenReturn(Optional.empty());
        when(ratingRepository.findByBookId("b1"))
                .thenReturn(List.of(rating(4), rating(5), rating(4)));

        ratingService.rate("u1", "b1", 4);

        ArgumentCaptor<Update> updateCaptor = ArgumentCaptor.forClass(Update.class);
        verify(mongoTemplate).updateFirst(any(Query.class), updateCaptor.capture(), eq(Book.class));

        Update update = updateCaptor.getValue();
        // avg of 4,5,4 = 4.333 -> rounded to 4.3; count = 3
        assertThat(update.getUpdateObject().get("$set")).isNotNull();
        assertThat(update.getUpdateObject().toBsonDocument().getDocument("$set")
                .getDouble("ratingAvg").getValue()).isEqualTo(4.3);
        assertThat(update.getUpdateObject().toBsonDocument().getDocument("$set")
                .getInt32("ratingCount").getValue()).isEqualTo(3);
    }
}
