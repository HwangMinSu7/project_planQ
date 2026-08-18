package com.example.ex76.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "member_badge", uniqueConstraints =
    @UniqueConstraint(name = "uk_member_badge", columnNames = {"member_email", "badge_id"}))
public class MemberBadge {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "member_email", nullable = false)
  private ClubMember member;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "badge_id", nullable = false)
  private Badge badge;

  @Column(nullable = false)
  private LocalDateTime earnedAt;
}
