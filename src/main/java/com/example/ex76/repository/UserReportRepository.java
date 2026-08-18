package com.example.ex76.repository;

import com.example.ex76.entity.ReportStatus;
import com.example.ex76.entity.UserReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserReportRepository extends JpaRepository<UserReport, Long> {
  @EntityGraph(attributePaths = "reporter")
  Page<UserReport> findAllByOrderByIdDesc(Pageable pageable);

  @EntityGraph(attributePaths = "reporter")
  Page<UserReport> findByStatusOrderByIdDesc(ReportStatus status, Pageable pageable);
}
