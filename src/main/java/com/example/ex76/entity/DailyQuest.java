package com.example.ex76.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "daily_quest", uniqueConstraints =
    @UniqueConstraint(name = "uk_daily_quest_member_date", columnNames = {"member_email", "quest_date"}))
public class DailyQuest extends BasicEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "member_email", nullable = false)
  private ClubMember member;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "mission_id", nullable = false)
  private Mission mission;

  @Column(name = "quest_date", nullable = false)
  private LocalDate questDate;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  @Builder.Default
  private QuestStatus status = QuestStatus.ASSIGNED;

  @Builder.Default
  private boolean rerolled = false;

  @Lob
  private String note;

  @Column(length = 600)
  private String proofImagePath;

  private LocalDateTime completedAt;

  public void reroll(Mission newMission) {
    if (status == QuestStatus.COMPLETED) throw new IllegalStateException("완료한 퀘스트는 바꿀 수 없습니다.");
    if (rerolled) throw new IllegalStateException("퀘스트 교체는 하루에 한 번만 가능합니다.");
    this.mission = newMission;
    this.rerolled = true;
  }

  public void complete(String note, String proofImagePath) {
    if (status == QuestStatus.COMPLETED) throw new IllegalStateException("이미 완료한 퀘스트입니다.");
    this.note = note == null ? null : note.trim();
    this.proofImagePath = proofImagePath;
    this.status = QuestStatus.COMPLETED;
    this.completedAt = LocalDateTime.now();
  }

  public boolean isCompleted() {
    return status == QuestStatus.COMPLETED;
  }
}
