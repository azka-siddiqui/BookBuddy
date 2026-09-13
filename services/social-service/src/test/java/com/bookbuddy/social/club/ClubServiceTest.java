package com.bookbuddy.social.club;

import com.bookbuddy.social.book.BookViewRepository;
import com.bookbuddy.social.reading.ReadingProgressRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClubServiceTest {

    @Mock
    private BookClubRepository clubRepository;
    @Mock
    private ClubMembershipRepository membershipRepository;
    @Mock
    private ReadingProgressRepository progressRepository;
    @Mock
    private BookViewRepository bookViewRepository;

    @InjectMocks
    private ClubService clubService;

    private BookClub club(int members, int capacity) {
        BookClub c = new BookClub();
        c.setId("c1");
        c.setName("Book Club 38");
        c.setCapacity(capacity);
        c.setMemberCount(members);
        c.setVersion(0L);
        return c;
    }

    @Test
    void joinSucceedsWhenSpaceAvailable() {
        when(membershipRepository.existsByClubIdAndUserId("c1", "u1")).thenReturn(false);
        when(clubRepository.findById("c1")).thenReturn(Optional.of(club(19, 20)));
        when(clubRepository.save(any(BookClub.class))).thenAnswer(inv -> inv.getArgument(0));

        BookClub result = clubService.join("c1", "u1");

        assertThat(result.getMemberCount()).isEqualTo(20);
        verify(membershipRepository).save(any());
    }

    @Test
    void joinRejectedWhenClubFull() {
        when(membershipRepository.existsByClubIdAndUserId("c1", "u1")).thenReturn(false);
        when(clubRepository.findById("c1")).thenReturn(Optional.of(club(20, 20)));

        assertThatThrownBy(() -> clubService.join("c1", "u1"))
                .isInstanceOf(ClubFullException.class);

        verify(clubRepository, never()).save(any());
        verify(membershipRepository, never()).save(any());
    }

    @Test
    void joinIsIdempotentForExistingMember() {
        when(membershipRepository.existsByClubIdAndUserId("c1", "u1")).thenReturn(true);
        when(clubRepository.findById("c1")).thenReturn(Optional.of(club(5, 20)));

        BookClub result = clubService.join("c1", "u1");

        assertThat(result.getMemberCount()).isEqualTo(5);
        verify(clubRepository, never()).save(any());
        verify(membershipRepository, never()).save(any());
    }

    @Test
    void joinRetriesOnOptimisticLockConflictThenSucceeds() {
        when(membershipRepository.existsByClubIdAndUserId("c1", "u1")).thenReturn(false);
        when(clubRepository.findById("c1")).thenReturn(Optional.of(club(10, 20)));
        // First save conflicts, second succeeds.
        when(clubRepository.save(any(BookClub.class)))
                .thenThrow(new OptimisticLockingFailureException("conflict"))
                .thenAnswer(inv -> inv.getArgument(0));

        BookClub result = clubService.join("c1", "u1");

        assertThat(result).isNotNull();
        verify(clubRepository, times(2)).save(any());
    }
}
