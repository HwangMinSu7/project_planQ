package com.example.ex76.repository;

import com.example.ex76.entity.MissionSuggestion;
import com.example.ex76.entity.SuggestionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.time.LocalDateTime;
import java.util.List;

public interface MissionSuggestionRepository extends JpaRepository<MissionSuggestion, Long> {
  @EntityGraph(attributePaths = {"author", "performer", "applicant"})
  Page<MissionSuggestion> findAllByOrderByIdDesc(Pageable pageable);

  @EntityGraph(attributePaths = {"author", "performer", "applicant"})
  Page<MissionSuggestion> findByStatusOrderByIdDesc(SuggestionStatus status, Pageable pageable);

  @EntityGraph(attributePaths = {"author", "performer", "applicant"})
  Optional<MissionSuggestion> findDetailById(Long id);

  @EntityGraph(attributePaths = {"author", "applicant"})
  List<MissionSuggestion> findByPerformerIsNullAndApplicantIsNotNullAndAcceptRequestedAtBefore(
      LocalDateTime cutoff);
}
