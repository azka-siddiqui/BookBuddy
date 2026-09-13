package com.bookbuddy.social.reading;

import com.bookbuddy.social.auth.AuthenticatedUser;
import com.bookbuddy.social.book.BookView;
import com.bookbuddy.social.book.BookViewRepository;
import com.bookbuddy.social.reading.dto.CompletionRate;
import com.bookbuddy.social.reading.dto.ProfileSummary;
import com.bookbuddy.social.reading.dto.ProgressRequest;
import com.bookbuddy.social.reading.dto.ProgressResponse;
import com.bookbuddy.social.user.User;
import com.bookbuddy.social.user.UserRepository;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Profile / reading-journey endpoints backing the profile page: summary tiles,
 * status-filtered book lists, progress updates, and the book completion rate (R13).
 */
@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ReadingProgressService progressService;
    private final BookViewRepository bookViewRepository;
    private final UserRepository userRepository;

    public ProfileController(ReadingProgressService progressService,
                             BookViewRepository bookViewRepository,
                             UserRepository userRepository) {
        this.progressService = progressService;
        this.bookViewRepository = bookViewRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/summary")
    public ProfileSummary summary(@AuthenticationPrincipal AuthenticatedUser principal) {
        int streak = userRepository.findById(principal.userId())
                .map(User::getStreakCount)
                .orElse(0);
        return progressService.summaryFor(principal.userId(), streak);
    }

    /**
     * Returns the user's tracked books, optionally filtered by status. Backs the
     * Wishlist / In Progress / Finished / Reading History tabs on the profile page.
     */
    @GetMapping("/books")
    public List<ProgressResponse> books(@AuthenticationPrincipal AuthenticatedUser principal,
                                        @RequestParam(required = false) String status) {
        List<ReadingProgress> progressList = (status == null || status.isBlank())
                ? progressService.forUser(principal.userId())
                : progressService.forUserByStatus(principal.userId(),
                        ReadingStatus.valueOf(status.toUpperCase()));

        Map<String, BookView> books = loadBooks(progressList.stream()
                .map(ReadingProgress::getBookId).toList());

        return progressList.stream()
                .map(p -> ProgressResponse.from(p, books.get(p.getBookId())))
                .toList();
    }

    @PostMapping("/progress")
    public ProgressResponse updateProgress(@AuthenticationPrincipal AuthenticatedUser principal,
                                           @Valid @RequestBody ProgressRequest request) {
        ReadingStatus status = ReadingStatus.valueOf(request.status().toUpperCase());
        ReadingProgress saved = progressService.upsertProgress(
                principal.userId(), request.bookId(), status, request.pageReached());
        BookView book = bookViewRepository.findById(saved.getBookId()).orElse(null);
        return ProgressResponse.from(saved, book);
    }

    /** Book completion rate (R13). */
    @GetMapping("/books/{bookId}/completion-rate")
    public CompletionRate completionRate(@PathVariable String bookId) {
        return progressService.completionRate(bookId);
    }

    private Map<String, BookView> loadBooks(List<String> bookIds) {
        return bookViewRepository.findAllById(bookIds).stream()
                .collect(Collectors.toMap(BookView::getId, Function.identity()));
    }
}
