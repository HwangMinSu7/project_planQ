package com.example.ex76.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "board_comment")
public class BoardComment extends BasicEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "post_id", nullable = false)
  private BoardPost post;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "author_email", nullable = false)
  private ClubMember author;

  @Column(nullable = false, length = 600)
  private String content;

  @Builder.Default
  private int likeCount = 0;

  @Builder.Default
  private boolean pinned = false;

  @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<CommentLike> likes = new ArrayList<>();

  public void addLike() { likeCount++; }
  public void removeLike() { likeCount = Math.max(0, likeCount - 1); }
  public void pin() { pinned = true; }
  public void unpin() { pinned = false; }
}
