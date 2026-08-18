package com.example.ex76.repository;

import com.example.ex76.entity.MeetupParticipant;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

public interface MeetupParticipantRepository extends JpaRepository<MeetupParticipant, Long> {
  boolean existsByPost_IdAndMember_Email(Long postId, String email);
  Optional<MeetupParticipant> findByPost_IdAndMember_Email(Long postId, String email);

  @EntityGraph(attributePaths = "member")
  List<MeetupParticipant> findByPost_IdOrderByJoinedAtAsc(Long postId);

  long countByMember_Email(String email);

  @EntityGraph(attributePaths = {"post", "post.author"})
  List<MeetupParticipant> findTop5ByMember_EmailAndPost_MeetingAtAfterOrderByPost_MeetingAtAsc(
      String email, LocalDateTime now);
}
