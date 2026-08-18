package com.example.ex76.repository;

import com.example.ex76.entity.Mission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MissionRepository extends JpaRepository<Mission, Long> {
  List<Mission> findByActiveTrue();
  List<Mission> findByActiveTrueAndWebFriendlyTrue();
  boolean existsByTitle(String title);
}
