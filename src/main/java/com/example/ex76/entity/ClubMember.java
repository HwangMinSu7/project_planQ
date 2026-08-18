package com.example.ex76.entity;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Version;
import lombok.*;

import java.util.HashSet;
import java.time.LocalDate;
import java.util.Set;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
public class ClubMember extends BasicEntity {

  @Id
  private String email;

  private String password;
  private String name;
  private boolean fromSocial;

  @Column(length = 500)
  private String profileImagePath;

  @Enumerated(EnumType.STRING)
  @Column(length = 40)
  private BadgeCode featuredBadgeCode;

  @Builder.Default
  private int rewardPoints = 0;

  @Builder.Default
  private int level = 1;

  @Builder.Default
  private int currentStreak = 0;

  @Builder.Default
  private int longestStreak = 0;

  private LocalDate lastQuestCompletedDate;

  @Version
  private Long version;

  @ElementCollection(fetch = FetchType.LAZY)
  @Builder.Default
  private Set<ClubMemberRole> roleSet = new HashSet<>();

  public void addMemberRole(ClubMemberRole role) {
    roleSet.add(role);
  }

  public void changePassword(String newPassword) {
    this.password = newPassword;
  }
  public void changeName(String name) { this.name = name; }
  public void changeProfileImage(String profileImagePath) {
    this.profileImagePath = profileImagePath;
  }
  public void changeFeaturedBadge(BadgeCode badgeCode) {
    this.featuredBadgeCode = badgeCode;
  }

  public int rewardQuest(LocalDate completedDate, int basePoints) {
    if (completedDate.equals(lastQuestCompletedDate)) return 0;

    if (lastQuestCompletedDate != null
        && lastQuestCompletedDate.plusDays(1).equals(completedDate)) {
      currentStreak++;
    } else {
      currentStreak = 1;
    }

    longestStreak = Math.max(longestStreak, currentStreak);
    int streakBonus = Math.min((currentStreak - 1) * 10, 100);
    int earnedPoints = basePoints + streakBonus;
    rewardPoints += earnedPoints;
    level = (rewardPoints / 500) + 1;
    lastQuestCompletedDate = completedDate;
    return earnedPoints;
  }

  public void initializeRewardProfile() {
    if (level < 1) level = 1;
  }

  public void spendPoints(int points) {
    if (points < 0) throw new IllegalArgumentException("포인트는 0 이상이어야 합니다.");
    if (rewardPoints < points) throw new IllegalStateException("보유 포인트가 부족합니다.");
    rewardPoints -= points;
    level = Math.max(1, (rewardPoints / 500) + 1);
  }

  public void addPoints(int points) {
    if (points < 0) throw new IllegalArgumentException("포인트는 0 이상이어야 합니다.");
    rewardPoints += points;
    level = (rewardPoints / 500) + 1;
  }
}
