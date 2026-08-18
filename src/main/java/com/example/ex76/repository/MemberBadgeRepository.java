package com.example.ex76.repository;

import com.example.ex76.entity.BadgeCode;
import com.example.ex76.entity.MemberBadge;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberBadgeRepository extends JpaRepository<MemberBadge, Long> {
  boolean existsByMember_EmailAndBadge_Code(String email, BadgeCode code);

  @EntityGraph(attributePaths = "badge")
  List<MemberBadge> findByMember_EmailOrderByEarnedAtDesc(String email);
}
