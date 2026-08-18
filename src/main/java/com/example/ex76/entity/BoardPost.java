package com.example.ex76.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "board_post")
public class BoardPost extends BasicEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "author_email", nullable = false)
  private ClubMember author;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private BoardCategory category;

  @Column(nullable = false, length = 160)
  private String title;

  @Lob
  @Column(nullable = false)
  private String content;

  @Column(length = 600)
  private String imagePath;

  private LocalDateTime meetingAt;

  @Column(length = 150)
  private String meetingPlace;

  private Integer maxParticipants;

  @Builder.Default
  private int participantCount = 0;

  @Builder.Default
  private int commentCount = 0;

  @Builder.Default
  private int likeCount = 0;

  @Builder.Default
  private boolean pinned = false;

  @Builder.Default
  private int pinOrder = 0;

  @Version
  private Long version;

  @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<BoardComment> comments = new ArrayList<>();

  @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<MeetupParticipant> participants = new ArrayList<>();

  @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<PostLike> likes = new ArrayList<>();

  public void update(BoardCategory category, String title, String content,
                     LocalDateTime meetingAt, String meetingPlace, Integer maxParticipants) {
    this.category = category;
    this.title = title;
    this.content = content;
    if (category == BoardCategory.MEETUP) {
      this.meetingAt = meetingAt;
      this.meetingPlace = meetingPlace;
      this.maxParticipants = maxParticipants;
    } else {
      this.meetingAt = null;
      this.meetingPlace = null;
      this.maxParticipants = null;
    }
  }

  public void changeImage(String imagePath) { this.imagePath = imagePath; }

  public void addComment() { commentCount++; }
  public void removeComment() { commentCount = Math.max(0, commentCount - 1); }
  public void addParticipant() { participantCount++; }
  public void removeParticipant() { participantCount = Math.max(0, participantCount - 1); }
  public void addLike() { likeCount++; }
  public void removeLike() { likeCount = Math.max(0, likeCount - 1); }
  public void pin() { pinned = true; }
  public void pin(int order) { pinned = true; pinOrder = order; }
  public void unpin() { pinned = false; pinOrder = 0; }
  public void changePinOrder(int order) { pinOrder = order; }
}
