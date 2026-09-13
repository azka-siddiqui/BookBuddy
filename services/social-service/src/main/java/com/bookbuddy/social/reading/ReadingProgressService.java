package com.bookbuddy.social.reading;

import com.bookbuddy.social.book.BookView;
import com.bookbuddy.social.book.BookViewRepository;
import com.bookbuddy.social.reading.dto.CompletionRate;
import com.bookbuddy.social.reading.dto.ProfileSummary;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Manages a user's reading journey: tracking progress, transitioning status, and
 * deriving the profile summary (R15) and per-book completion rate (R13). Recording
 * progress on a book also registers a reading event that advances the user's streak.
 */
@Service
public class ReadingProgressService {

    private final ReadingProgressRepository progressRepository;
    private final BookViewRepository bookViewRepository;
    private final StreakService streakService;

    public ReadingProgressService(ReadingProgressRepository progressRepository,
                                  BookViewRepository bookViewRepository,
                                  StreakService streakService) {
        this.progressRepository = progressRepository;
        this.bookViewRepository = bookViewRepository;
        this.streakService = streakService;
    }

    public List<ReadingProgress> forUser(String userId) {
        return progressRepository.findByUserId(userId);
    }

    public List<ReadingProgress> forUserByStatus(String userId, ReadingStatus status) {
        return progressRepository.findByUserIdAndStatus(userId, status);
    }

    /**
     * Adds or updates the user's progress on a book. Moving to IN_PROGRESS or
     * FINISHED registers a reading event that advances the streak.
     */
    public ReadingProgress upsertProgress(String userId, String bookId,
                                          ReadingStatus status, int pageReached) {
        Instant now = Instant.now();

        ReadingProgress progress = progressRepository.findByUserIdAndBookId(userId, bookId)
                .orElseGet(() -> {
                    ReadingProgress p = new ReadingProgress();
                    p.setId(UUID.randomUUID().toString());
                    p.setUserId(userId);
                    p.setBookId(bookId);
                    p.setTotalPages(resolveTotalPages(bookId));
                    return p;
                });

        applyStatusTransition(progress, status, now);
        progress.setPageReached(pageReached);
        progress.setUpdatedAt(now);
        ReadingProgress saved = progressRepository.save(progress);

        if (status == ReadingStatus.IN_PROGRESS || status == ReadingStatus.FINISHED) {
            streakService.recordReadingEvent(userId, now);
        }
        return saved;
    }

    /** Profile header tiles: totals plus the current streak (R15). */
    public ProfileSummary summaryFor(String userId, int streakCount) {
        long total = progressRepository.countByUserId(userId);
        long inProgress = progressRepository.countByUserIdAndStatus(userId, ReadingStatus.IN_PROGRESS);
        long completed = progressRepository.countByUserIdAndStatus(userId, ReadingStatus.FINISHED);
        return new ProfileSummary(total, inProgress, completed, streakCount);
    }

    /**
     * Completion rate for a book (R13): of readers who started (IN_PROGRESS or
     * FINISHED), the fraction who finished.
     */
    public CompletionRate completionRate(String bookId) {
        long finished = progressRepository.countByBookIdAndStatus(bookId, ReadingStatus.FINISHED);
        long inProgress = progressRepository.countByBookIdAndStatus(bookId, ReadingStatus.IN_PROGRESS);
        long started = finished + inProgress;
        double rate = started == 0 ? 0.0 : (double) finished / started;
        return new CompletionRate(bookId, started, finished, round2(rate));
    }

    private void applyStatusTransition(ReadingProgress progress, ReadingStatus target, Instant now) {
        ReadingStatus previous = progress.getStatus();
        progress.setStatus(target);

        if (target == ReadingStatus.IN_PROGRESS && progress.getStartedAt() == null) {
            progress.setStartedAt(now);
        }
        if (target == ReadingStatus.FINISHED) {
            if (progress.getStartedAt() == null) {
                progress.setStartedAt(now);
            }
            progress.setFinishedAt(now);
            if (progress.getTotalPages() > 0) {
                progress.setPageReached(progress.getTotalPages());
            }
        }
        // Moving back to WISHLIST clears finished timestamp.
        if (target == ReadingStatus.WISHLIST && previous == ReadingStatus.FINISHED) {
            progress.setFinishedAt(null);
        }
    }

    private int resolveTotalPages(String bookId) {
        return bookViewRepository.findById(bookId)
                .map(BookView::getPageCount)
                .orElse(0);
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
