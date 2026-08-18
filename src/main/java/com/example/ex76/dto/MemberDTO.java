package com.example.ex76.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberDTO {
  private Long mid;

  private String email;
  private String pw;
  private String nickname;

  private LocalDateTime regDate, modDate;
}
