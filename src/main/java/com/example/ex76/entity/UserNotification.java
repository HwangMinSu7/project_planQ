package com.example.ex76.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_notification")
public class UserNotification extends BasicEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "recipient_email", nullable = false)
  private ClubMember recipient;

  @Column(nullable = false, length = 300)
  private String message;

  @Column(nullable = false, length = 300)
  private String linkUrl;

  @Column(name = "is_read", nullable = false)
  @Builder.Default
  private boolean read = false;

  public void markRead() { read = true; }
}
