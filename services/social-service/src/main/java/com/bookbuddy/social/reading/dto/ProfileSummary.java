package com.bookbuddy.social.reading.dto;

/**
 * Aggregated stats shown on the profile header tiles.
 *
 * @param totalBooks total books tracked by the user
 * @param inProgress books currently being read
 * @param completed  books finished
 * @param dayStreak  current consecutive-day reading streak (R15)
 */
public record ProfileSummary(long totalBooks, long inProgress, long completed, int dayStreak) {
}
