package com.bookbuddy.social.club;

import com.bookbuddy.social.auth.AuthenticatedUser;
import com.bookbuddy.social.club.dto.ClubRecommendation;
import com.bookbuddy.social.club.dto.ClubResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;

/**
 * Book club endpoints: browse clubs, join a club (capacity-limited, R14), and get
 * genre-based club recommendations (R12).
 */
@RestController
@RequestMapping("/api/clubs")
public class ClubController {

    private final ClubService clubService;

    public ClubController(ClubService clubService) {
        this.clubService = clubService;
    }

    @GetMapping
    public List<ClubResponse> list() {
        return clubService.allClubs().stream().map(ClubResponse::from).toList();
    }

    /** Join a club. Returns the updated club, or an error if it is full (R14). */
    @PostMapping("/{clubId}/join")
    public ClubResponse join(@AuthenticationPrincipal AuthenticatedUser principal,
                             @PathVariable String clubId) {
        return ClubResponse.from(clubService.join(clubId, principal.userId()));
    }

    /** Clubs recommended for the current user based on reading genres (R12). */
    @GetMapping("/recommendations")
    public List<ClubRecommendation> recommendations(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @RequestParam(defaultValue = "5") int limit) {
        return clubService.recommendClubs(principal.userId(), limit).stream()
                .map(ClubRecommendation::from)
                .toList();
    }
}
