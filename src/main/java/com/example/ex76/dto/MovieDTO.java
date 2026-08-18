package com.example.ex76.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MovieDTO {
  private Long mno;
  private String title;

  @Builder.Default //@Builder는 null로 초기화, @Builder.Default는 new ArrayList<>()로 초기화
  private List<MovieImageDTO> imageDTOList = new ArrayList<>();

  private double avg;
  private int reviewCnt;
  private LocalDateTime regDate;
  private LocalDateTime modDate;
}
