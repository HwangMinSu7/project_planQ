package com.example.ex76.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_report")
public class UserReport extends BasicEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "reporter_email", nullable = false)
  private ClubMember reporter;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private ReportKind kind;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private ReportTargetType targetType;

  private Long targetId;

  @Column(nullable = false, length = 160)
  private String targetTitle;

  @Column(nullable = false, length = 1000)
  private String reason;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  @Builder.Default
  private ReportStatus status = ReportStatus.PENDING;

  @Column(length = 100)
  private String reviewerEmail;
  private LocalDateTime reviewedAt;

  public void review(ReportStatus status, String reviewerEmail) {
    if (status == null || status == ReportStatus.PENDING) {
      throw new IllegalArgumentException("처리 결과를 선택해 주세요.");
    }
    this.status = status;
    this.reviewerEmail = reviewerEmail;
    this.reviewedAt = LocalDateTime.now();
  }
}
