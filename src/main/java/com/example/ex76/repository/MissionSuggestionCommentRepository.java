package com.example.ex76.repository;

import com.example.ex76.entity.MissionSuggestionComment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MissionSuggestionCommentRepository extends JpaRepository<MissionSuggestionComment, Long> {
  @EntityGraph(attributePaths = "author")
  List<MissionSuggestionComment> findBySuggestion_IdOrderByIdAsc(Long suggestionId);
}
