package com.bookbuddy.social.recommend;

import com.bookbuddy.social.book.BookView;
import com.bookbuddy.social.book.BookViewRepository;
import com.bookbuddy.social.reading.ReadingProgress;
import com.bookbuddy.social.reading.ReadingProgressRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private ReadingProgressRepository progressRepository;
    @Mock
    private BookViewRepository bookViewRepository;

    @InjectMocks
    private RecommendationService service;

    private ReadingProgress progress(String userId, String bookId) {
        ReadingProgress p = new ReadingProgress();
        p.setUserId(userId);
        p.setBookId(bookId);
        return p;
    }

    private BookView book(String id, String title, List<String> tags) {
        // Build via reflection-free JSON-like stub: use a small anonymous subclass.
        return new BookView() {
            @Override
            public String getId() {
                return id;
            }

            @Override
            public String getTitle() {
                return title;
            }

            @Override
            public List<String> getTags() {
                return tags;
            }
        };
    }

    @Test
    void recommendsBooksSharingPreferredTagsExcludingTracked() {
        // User tracks one Fantasy book (b1).
        when(progressRepository.findByUserId("u1"))
                .thenReturn(List.of(progress("u1", "b1")));
        BookView tracked = book("b1", "Tracked Fantasy", List.of("Fantasy"));
        when(bookViewRepository.findAllById(any(Iterable.class)))
                .thenReturn(List.of(tracked));

        BookView candidateFantasy = book("b2", "Another Fantasy", List.of("Fantasy"));
        BookView candidateUnrelated = book("b3", "Cooking", List.of("Food"));
        when(bookViewRepository.findByTagsIn(anyList()))
                .thenReturn(List.of(tracked, candidateFantasy, candidateUnrelated));

        List<BookView> recs = service.recommendBooks("u1", 6);

        assertThat(recs).extracting(BookView::getId).containsExactly("b2");
    }

    @Test
    void returnsEmptyWhenUserHasNoTrackedBooks() {
        when(progressRepository.findByUserId("u1")).thenReturn(List.of());
        lenient().when(bookViewRepository.findAllById(any(Iterable.class))).thenReturn(List.of());

        assertThat(service.recommendBooks("u1", 6)).isEmpty();
    }

    @Test
    void commonBooksReturnsIntersection() {
        when(progressRepository.findByUserId("u1"))
                .thenReturn(List.of(progress("u1", "b1"), progress("u1", "b2")));
        when(progressRepository.findByUserId("u2"))
                .thenReturn(List.of(progress("u2", "b2"), progress("u2", "b3")));
        when(bookViewRepository.findAllById(any(Iterable.class)))
                .thenReturn(List.of(book("b2", "Shared Book", List.of("Fantasy"))));

        List<BookView> common = service.commonBooks("u1", "u2");

        assertThat(common).extracting(BookView::getId).containsExactly("b2");
    }
}
