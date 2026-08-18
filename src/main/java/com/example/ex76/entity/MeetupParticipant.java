package com.example.ex76.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "meetup_participant", uniqueConstraints =
    @UniqueConstraint(name = "uk_meetup_post_member", columnNames = {"post_id", "member_email"}))
public class MeetupParticipant {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "post_id", nullable = false)
  private BoardPost post;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "member_email", nullable = false)
  private ClubMember member;

  @Column(nullable = false)
  private LocalDateTime joinedAt;
}
