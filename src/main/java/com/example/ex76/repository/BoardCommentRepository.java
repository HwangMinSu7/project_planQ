package com.example.ex76.repository;

import com.example.ex76.entity.BoardComment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardCommentRepository extends JpaRepository<BoardComment, Long> {
  @EntityGraph(attributePaths = "author")
  List<BoardComment> findByPost_IdOrderByPinnedDescIdAsc(Long postId);

  List<BoardComment> findByPost_IdAndPinnedTrue(Long postId);

  long countByAuthor_Email(String email);

  @EntityGraph(attributePaths = {"post", "author"})
  List<BoardComment> findTop5ByAuthor_EmailOrderByIdDesc(String email);
}
