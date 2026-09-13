package com.bookbuddy.social.recommend;

import com.bookbuddy.social.book.BookView;
import com.bookbuddy.social.book.BookViewRepository;
import com.bookbuddy.social.reading.ReadingProgress;
import com.bookbuddy.social.reading.ReadingProgressRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Produces personalized book recommendations (R11).
 *
 * <p>Approach: derive the tags the user reads most from their tracked books, then
 * score candidate books that share those tags by how many preferred tags they
 * match (weighted by the user's affinity for each tag), excluding books the user
 * already tracks. This "readers who like these tags also read…" heuristic is simple,
 * explainable, and works on the demo dataset.
 */
@Service
public class RecommendationService {

    private static final int DEFAULT_LIMIT = 6;

    private final ReadingProgressRepository progressRepository;
    private final BookViewRepository bookViewRepository;

    public RecommendationService(ReadingProgressRepository progressRepository,
                                 BookViewRepository bookViewRepository) {
        this.progressRepository = progressRepository;
        this.bookViewRepository = bookViewRepository;
    }

    public List<BookView> recommendBooks(String userId, int limit) {
        int cap = limit <= 0 ? DEFAULT_LIMIT : Math.min(limit, 50);

        List<ReadingProgress> tracked = progressRepository.findByUserId(userId);
        Set<String> trackedBookIds = tracked.stream()
                .map(ReadingProgress::getBookId)
                .collect(Collectors.toSet());

        Map<String, Integer> tagAffinity = computeTagAffinity(trackedBookIds);
        if (tagAffinity.isEmpty()) {
            return List.of();
        }

        List<BookView> candidates = bookViewRepository.findByTagsIn(List.copyOf(tagAffinity.keySet()));

        return candidates.stream()
                .filter(book -> !trackedBookIds.contains(book.getId()))
                .map(book -> Map.entry(book, score(book, tagAffinity)))
                .filter(e -> e.getValue() > 0)
                .sorted(Map.Entry.<BookView, Integer>comparingByValue().reversed())
                .limit(cap)
                .map(Map.Entry::getKey)
                .toList();
    }

    /**
     * Builds a map of tag -> affinity, where affinity is the number of the user's
     * tracked books carrying that tag.
     */
    private Map<String, Integer> computeTagAffinity(Set<String> trackedBookIds) {
        Map<String, Integer> affinity = new HashMap<>();
        for (BookView book : bookViewRepository.findAllById(trackedBookIds)) {
            for (String tag : book.getTags()) {
                affinity.merge(tag, 1, Integer::sum);
            }
        }
        return affinity;
    }

    /** Score a candidate by summing the user's affinity for each shared tag. */
    private int score(BookView candidate, Map<String, Integer> tagAffinity) {
        return candidate.getTags().stream()
                .mapToInt(tag -> tagAffinity.getOrDefault(tag, 0))
                .sum();
    }

    /**
     * Books shared between two users (R8): the intersection of the books each has
     * tracked, resolved to full book details.
     */
    public List<BookView> commonBooks(String userIdA, String userIdB) {
        Set<String> a = progressRepository.findByUserId(userIdA).stream()
                .map(ReadingProgress::getBookId).collect(Collectors.toSet());
        Set<String> b = progressRepository.findByUserId(userIdB).stream()
                .map(ReadingProgress::getBookId).collect(Collectors.toSet());

        a.retainAll(b);
        if (a.isEmpty()) {
            return List.of();
        }
        return bookViewRepository.findAllById(a).stream()
                .sorted(Comparator.comparing(BookView::getTitle))
                .toList();
    }
}
