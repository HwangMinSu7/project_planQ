package com.example.ex76.repository;

import com.example.ex76.entity.Badge;
import com.example.ex76.entity.BadgeCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BadgeRepository extends JpaRepository<Badge, Long> {
  Optional<Badge> findByCode(BadgeCode code);
  boolean existsByCode(BadgeCode code);
}
