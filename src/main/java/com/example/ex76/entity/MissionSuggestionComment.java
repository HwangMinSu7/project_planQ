package com.example.ex76.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "mission_suggestion_comment")
public class MissionSuggestionComment extends BasicEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "suggestion_id", nullable = false)
  private MissionSuggestion suggestion;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "author_email", nullable = false)
  private ClubMember author;

  @Column(nullable = false, length = 600)
  private String content;
}
