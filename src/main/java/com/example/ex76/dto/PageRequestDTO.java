package com.example.ex76.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Builder
@AllArgsConstructor
@Data
// 페이지 요청을 위한 객체
public class PageRequestDTO {
  private int page;
  private int size;
  private String type;
  private String keyword;

  public PageRequestDTO() {
    page = 1;
    size = 10;
  }

  public Pageable getPageable(Sort sort) {
    return PageRequest.of(page - 1, size, sort);
  }
}
