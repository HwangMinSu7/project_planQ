package com.example.ex76.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "mission_suggestion")
public class MissionSuggestion extends BasicEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "author_email", nullable = false)
  private ClubMember author;

  @Column(nullable = false, length = 120)
  private String title;

  @Column(nullable = false, length = 500)
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private MissionCategory category;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  @Builder.Default
  private SuggestionStatus status = SuggestionStatus.PENDING;

  @Column(length = 100)
  private String reviewerEmail;

  private LocalDateTime reviewedAt;

  @Builder.Default
  private int bountyPoints = 0;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "performer_email")
  private ClubMember performer;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "applicant_email")
  private ClubMember applicant;

  private LocalDateTime acceptRequestedAt;

  private LocalDateTime acceptedAt;

  @Builder.Default
  private boolean authorCompleted = false;

  @Builder.Default
  private boolean performerCompleted = false;

  @Builder.Default
  private boolean pointsSettled = false;

  private LocalDateTime settledAt;

  public void approve(String reviewerEmail) {
    ensurePending();
    this.status = SuggestionStatus.APPROVED;
    this.reviewerEmail = reviewerEmail;
    this.reviewedAt = LocalDateTime.now();
  }

  public void reject(String reviewerEmail) {
    ensurePending();
    this.status = SuggestionStatus.REJECTED;
    this.reviewerEmail = reviewerEmail;
    this.reviewedAt = LocalDateTime.now();
  }

  public void requestAcceptance(ClubMember applicant) {
    if (status != SuggestionStatus.APPROVED) {
      throw new IllegalStateException("운영자가 승인한 미션만 수행할 수 있습니다.");
    }
    if (this.performer != null) throw new IllegalStateException("이미 수행자가 정해진 미션입니다.");
    if (this.applicant != null) throw new IllegalStateException("다른 회원의 수락 요청을 확인 중입니다.");
    if (author.getEmail().equals(applicant.getEmail())) {
      throw new IllegalStateException("자신이 제안한 포인트 미션은 직접 수행할 수 없습니다.");
    }
    this.applicant = applicant;
    this.acceptRequestedAt = LocalDateTime.now();
  }

  public ClubMember confirmAcceptance(String authorEmail) {
    if (!author.getEmail().equals(authorEmail)) {
      throw new IllegalStateException("미션 작성자만 수락 요청을 승인할 수 있습니다.");
    }
    if (applicant == null) throw new IllegalStateException("확인할 수락 요청이 없습니다.");
    ClubMember confirmed = applicant;
    this.performer = applicant;
    this.applicant = null;
    this.acceptRequestedAt = null;
    this.acceptedAt = LocalDateTime.now();
    return confirmed;
  }

  public ClubMember cancelAcceptanceRequest(String authorEmail) {
    if (!author.getEmail().equals(authorEmail)) {
      throw new IllegalStateException("미션 작성자만 수락 요청을 거절할 수 있습니다.");
    }
    return clearApplicant();
  }

  public ClubMember expireAcceptanceRequest() {
    return clearApplicant();
  }

  private ClubMember clearApplicant() {
    if (applicant == null) return null;
    ClubMember canceled = applicant;
    this.applicant = null;
    this.acceptRequestedAt = null;
    return canceled;
  }

  public void confirmCompletion(String email) {
    if (performer == null) throw new IllegalStateException("아직 수행자가 없습니다.");
    if (pointsSettled) throw new IllegalStateException("이미 포인트 교환이 완료되었습니다.");
    if (author.getEmail().equals(email)) authorCompleted = true;
    else if (performer.getEmail().equals(email)) performerCompleted = true;
    else throw new IllegalStateException("미션 당사자만 완료를 확인할 수 있습니다.");
  }

  public void settle() {
    if (pointsSettled) throw new IllegalStateException("이미 포인트를 지급했습니다.");
    if (performer == null || !authorCompleted || !performerCompleted) {
      throw new IllegalStateException("작성자와 수행자가 모두 완료를 눌러야 합니다.");
    }
    pointsSettled = true;
    settledAt = LocalDateTime.now();
  }

  private void ensurePending() {
    if (status != SuggestionStatus.PENDING) {
      throw new IllegalStateException("이미 검토가 끝난 제안입니다.");
    }
  }
}
