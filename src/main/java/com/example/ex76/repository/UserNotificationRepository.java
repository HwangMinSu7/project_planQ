package com.example.ex76.repository;

import com.example.ex76.entity.UserNotification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {
  long countByRecipient_EmailAndReadFalse(String email);

  @EntityGraph(attributePaths = "recipient")
  List<UserNotification> findTop30ByRecipient_EmailOrderByIdDesc(String email);
}
