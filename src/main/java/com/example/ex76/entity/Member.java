package com.example.ex76.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@Table(name = "m_member")
public class Member extends BasicEntity{
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long mid;

  private String email;
  private String pw;
  private String nickname;

  public void changePw(String pw) {this.pw = pw;}
  public void changeNickname(String nickname) {this.nickname = nickname;}
}
