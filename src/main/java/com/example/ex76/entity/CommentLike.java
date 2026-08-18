package com.example.ex76.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "comment_like", uniqueConstraints =
    @UniqueConstraint(name = "uk_comment_like_member", columnNames = {"comment_id", "member_email"}))
public class CommentLike {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "comment_id", nullable = false)
  private BoardComment comment;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "member_email", nullable = false)
  private ClubMember member;

  @Column(nullable = false)
  private LocalDateTime likedAt;
}
