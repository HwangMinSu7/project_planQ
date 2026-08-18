package com.example.ex76.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "badge", uniqueConstraints = @UniqueConstraint(name = "uk_badge_code", columnNames = "code"))
public class Badge {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 40)
  private BadgeCode code;

  @Column(nullable = false, length = 60)
  private String name;

  @Column(nullable = false, length = 240)
  private String description;

  @Column(nullable = false, length = 10)
  private String icon;
}
