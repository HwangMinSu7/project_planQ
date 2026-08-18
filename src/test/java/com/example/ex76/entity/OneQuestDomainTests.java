package com.example.ex76.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OneQuestDomainTests {

  @Test
  void consecutiveCompletionAddsStreakBonusAndLevel() {
    ClubMember member = ClubMember.builder()
        .email("quest@test.local").password("encoded").name("tester").build();
    LocalDate dayOne = LocalDate.of(2026, 8, 10);

    assertEquals(100, member.rewardQuest(dayOne, 100));
    assertEquals(110, member.rewardQuest(dayOne.plusDays(1), 100));
    assertEquals(120, member.rewardQuest(dayOne.plusDays(2), 100));
    assertEquals(3, member.getCurrentStreak());
    assertEquals(330, member.getRewardPoints());
    assertEquals(1, member.getLevel());
  }

  @Test
  void missedDayResetsCurrentStreak() {
    ClubMember member = ClubMember.builder()
        .email("quest2@test.local").password("encoded").name("tester").build();
    LocalDate first = LocalDate.of(2026, 8, 1);

    member.rewardQuest(first, 100);
    member.rewardQuest(first.plusDays(1), 100);
    assertEquals(100, member.rewardQuest(first.plusDays(4), 100));
    assertEquals(1, member.getCurrentStreak());
    assertEquals(2, member.getLongestStreak());
  }

  @Test
  void sameDateCannotReceiveRewardTwice() {
    ClubMember member = ClubMember.builder()
        .email("quest3@test.local").password("encoded").name("tester").build();
    LocalDate today = LocalDate.of(2026, 8, 13);

    assertEquals(100, member.rewardQuest(today, 100));
    assertEquals(0, member.rewardQuest(today, 100));
    assertEquals(100, member.getRewardPoints());
  }
}
