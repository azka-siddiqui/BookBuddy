package com.bookbuddy.social.recommend;

import com.bookbuddy.social.auth.AuthenticatedUser;
import com.bookbuddy.social.recommend.dto.BookSuggestion;
import com.bookbuddy.social.user.User;
import com.bookbuddy.social.user.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Recommendation endpoints: personalized book suggestions (R11) and the
 * common-books-between-two-users view (R8).
 */
@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final UserRepository userRepository;

    public RecommendationController(RecommendationService recommendationService,
                                    UserRepository userRepository) {
        this.recommendationService = recommendationService;
        this.userRepository = userRepository;
    }

    /** Personalized book recommendations for the current user (R11). */
    @GetMapping("/books")
    public List<BookSuggestion> recommendedBooks(@AuthenticationPrincipal AuthenticatedUser principal,
                                                 @RequestParam(defaultValue = "6") int limit) {
        return recommendationService.recommendBooks(principal.userId(), limit).stream()
                .map(BookSuggestion::from)
                .toList();
    }

    /**
     * Books the current user has in common with another user, looked up by
     * username (R8).
     */
    @GetMapping("/common")
    public ResponseEntity<List<BookSuggestion>> commonBooks(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @RequestParam String withUsername) {

        User other = userRepository.findByUsername(withUsername.trim().toLowerCase()).orElse(null);
        if (other == null) {
            return ResponseEntity.notFound().build();
        }
        List<BookSuggestion> common = recommendationService
                .commonBooks(principal.userId(), other.getId()).stream()
                .map(BookSuggestion::from)
                .toList();
        return ResponseEntity.ok(common);
    }
}
