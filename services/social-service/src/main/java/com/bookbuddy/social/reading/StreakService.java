package com.bookbuddy.social.reading;

import com.bookbuddy.social.user.User;
import com.bookbuddy.social.user.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

/**
 * Maintains each user's consecutive-day reading streak (R15).
 *
 * <p>Streak rules, evaluated in UTC calendar days:
 * <ul>
 *   <li>reading again on the <em>same</em> day: streak unchanged</li>
 *   <li>reading on the <em>next</em> day: streak + 1</li>
 *   <li>a gap of two or more days: streak resets to 1</li>
 *   <li>first ever reading event: streak becomes 1</li>
 * </ul>
 */
@Service
public class StreakService {

    private final UserRepository userRepository;

    public StreakService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Records a reading event for the user at the given instant and persists the
     * recomputed streak.
     *
     * @return the user's new streak count
     */
    public int recordReadingEvent(String userId, Instant eventTime) {
        User user = userRepository.findById(userId).orElseThrow();
        int newStreak = computeStreak(user.getStreakCount(), user.getLastReadDate(), eventTime);
        user.setStreakCount(newStreak);
        user.setLastReadDate(eventTime);
        userRepository.save(user);
        return newStreak;
    }

    /**
     * Pure streak computation, extracted for testability.
     *
     * @param currentStreak the streak before this event
     * @param lastReadDate  the previous reading instant (may be null)
     * @param eventTime     the new reading instant
     * @return the updated streak count
     */
    public int computeStreak(int currentStreak, Instant lastReadDate, Instant eventTime) {
        LocalDate today = toUtcDate(eventTime);
        if (lastReadDate == null) {
            return 1;
        }
        LocalDate last = toUtcDate(lastReadDate);

        if (last.equals(today)) {
            // Already read today; streak is at least 1 and unchanged.
            return Math.max(currentStreak, 1);
        }
        if (last.plusDays(1).equals(today)) {
            return Math.max(currentStreak, 0) + 1;
        }
        // Gap of two or more days (or an out-of-order event): restart the streak.
        return 1;
    }

    private LocalDate toUtcDate(Instant instant) {
        return instant.atZone(ZoneOffset.UTC).toLocalDate();
    }
}
