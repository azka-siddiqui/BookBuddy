package com.bookbuddy.social.club;

import com.bookbuddy.social.book.BookView;
import com.bookbuddy.social.book.BookViewRepository;
import com.bookbuddy.social.reading.ReadingProgress;
import com.bookbuddy.social.reading.ReadingProgressRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Book club membership and recommendations.
 *
 * <p>Joining is capacity-limited and safe under concurrency (R14): the club's
 * member count is incremented on a version-controlled document, so if two users
 * race for the final seat, Spring Data's optimistic locking causes one save to
 * fail. The failed attempt is retried, at which point it observes the club is
 * full and is rejected — guaranteeing capacity is never exceeded.
 */
@Service
public class ClubService {

    private static final Logger log = LoggerFactory.getLogger(ClubService.class);
    private static final int MAX_JOIN_RETRIES = 3;

    private final BookClubRepository clubRepository;
    private final ClubMembershipRepository membershipRepository;
    private final ReadingProgressRepository progressRepository;
    private final BookViewRepository bookViewRepository;

    public ClubService(BookClubRepository clubRepository,
                       ClubMembershipRepository membershipRepository,
                       ReadingProgressRepository progressRepository,
                       BookViewRepository bookViewRepository) {
        this.clubRepository = clubRepository;
        this.membershipRepository = membershipRepository;
        this.progressRepository = progressRepository;
        this.bookViewRepository = bookViewRepository;
    }

    public List<BookClub> allClubs() {
        return clubRepository.findAll();
    }

    /**
     * Attempts to add the user to the club.
     *
     * @return the updated club
     * @throws NoSuchElementException if the club does not exist
     * @throws ClubFullException      if the club is at capacity
     */
    public BookClub join(String clubId, String userId) {
        if (membershipRepository.existsByClubIdAndUserId(clubId, userId)) {
            // Idempotent: already a member.
            return clubRepository.findById(clubId).orElseThrow();
        }

        BookClub club = reserveSeat(clubId);

        // Seat reserved; record the membership. A duplicate here means a concurrent
        // request for the same user won the race — release the seat we just took.
        try {
            membershipRepository.save(new ClubMembership(
                    UUID.randomUUID().toString(), clubId, userId, Instant.now()));
        } catch (DuplicateKeyException e) {
            releaseSeat(clubId);
            return clubRepository.findById(clubId).orElseThrow();
        }
        return club;
    }

    /**
     * Atomically claims a seat using optimistic locking, retrying on version
     * conflicts. Capacity is re-checked on every attempt so the limit is never
     * breached under concurrent joins.
     */
    private BookClub reserveSeat(String clubId) {
        for (int attempt = 1; attempt <= MAX_JOIN_RETRIES; attempt++) {
            BookClub club = clubRepository.findById(clubId)
                    .orElseThrow(() -> new NoSuchElementException("Club not found: " + clubId));

            if (club.isFull()) {
                throw new ClubFullException(clubId);
            }

            club.setMemberCount(club.getMemberCount() + 1);
            try {
                return clubRepository.save(club);
            } catch (OptimisticLockingFailureException ex) {
                log.debug("Optimistic lock conflict joining club {} (attempt {}/{})",
                        clubId, attempt, MAX_JOIN_RETRIES);
            }
        }
        // Exhausted retries under heavy contention; treat as full for safety.
        throw new ClubFullException(clubId);
    }

    private void releaseSeat(String clubId) {
        clubRepository.findById(clubId).ifPresent(club -> {
            club.setMemberCount(Math.max(0, club.getMemberCount() - 1));
            try {
                clubRepository.save(club);
            } catch (OptimisticLockingFailureException ignored) {
                // Best-effort release; a concurrent writer will have a consistent count.
            }
        });
    }

    /**
     * Recommends clubs whose genres overlap the tags of the books the user reads (R12).
     * Clubs the user already belongs to are excluded, and results are ranked by the
     * size of the genre overlap.
     */
    public List<BookClub> recommendClubs(String userId, int limit) {
        int cap = limit <= 0 ? 5 : Math.min(limit, 20);

        Set<String> userTags = userReadingTags(userId);
        if (userTags.isEmpty()) {
            return List.of();
        }

        Set<String> joinedClubIds = membershipRepository.findByUserId(userId).stream()
                .map(ClubMembership::getClubId)
                .collect(Collectors.toSet());

        return clubRepository.findByGenresIn(List.copyOf(userTags)).stream()
                .filter(club -> !joinedClubIds.contains(club.getId()))
                .map(club -> Map.entry(club, overlap(club.getGenres(), userTags)))
                .filter(e -> e.getValue() > 0)
                .sorted(Map.Entry.<BookClub, Integer>comparingByValue().reversed())
                .limit(cap)
                .map(Map.Entry::getKey)
                .toList();
    }

    private Set<String> userReadingTags(String userId) {
        Set<String> bookIds = progressRepository.findByUserId(userId).stream()
                .map(ReadingProgress::getBookId)
                .collect(Collectors.toSet());
        Set<String> tags = new HashSet<>();
        for (BookView book : bookViewRepository.findAllById(bookIds)) {
            tags.addAll(book.getTags());
        }
        return tags;
    }

    private int overlap(List<String> genres, Set<String> userTags) {
        return (int) genres.stream().filter(userTags::contains).count();
    }
}
