package com.bookbuddy.social.reading;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

class StreakServiceTest {

    private final StreakService streakService = new StreakService(null);

    private static final Instant DAY_2_NOON =
            Instant.parse("2024-06-02T12:00:00Z");

    @Test
    void firstEverReadingStartsStreakAtOne() {
        assertThat(streakService.computeStreak(0, null, DAY_2_NOON)).isEqualTo(1);
    }

    @Test
    void readingAgainSameDayLeavesStreakUnchanged() {
        Instant earlierSameDay = Instant.parse("2024-06-02T08:00:00Z");
        assertThat(streakService.computeStreak(3, earlierSameDay, DAY_2_NOON)).isEqualTo(3);
    }

    @Test
    void readingNextDayIncrementsStreak() {
        Instant yesterday = DAY_2_NOON.minus(1, ChronoUnit.DAYS);
        assertThat(streakService.computeStreak(3, yesterday, DAY_2_NOON)).isEqualTo(4);
    }

    @Test
    void gapOfTwoOrMoreDaysResetsStreakToOne() {
        Instant threeDaysAgo = DAY_2_NOON.minus(3, ChronoUnit.DAYS);
        assertThat(streakService.computeStreak(9, threeDaysAgo, DAY_2_NOON)).isEqualTo(1);
    }

    @Test
    void sameDayWithZeroStreakBecomesOne() {
        Instant earlierSameDay = Instant.parse("2024-06-02T01:00:00Z");
        assertThat(streakService.computeStreak(0, earlierSameDay, DAY_2_NOON)).isEqualTo(1);
    }
}
