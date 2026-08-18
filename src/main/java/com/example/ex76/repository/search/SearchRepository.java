package com.example.ex76.repository.search;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SearchRepository {
  // SearchBoardRepository: 복수개의 엔티티를 검색하기 위해 별도의 interface로 분리
  Page<Object[]> searchPage(String type, String keyword, Pageable pageable);

}
