package com.example.ex76.repository;

import com.example.ex76.entity.DailyQuest;
import com.example.ex76.entity.QuestStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyQuestRepository extends JpaRepository<DailyQuest, Long> {
  @EntityGraph(attributePaths = {"mission", "member"})
  Optional<DailyQuest> findByMember_EmailAndQuestDate(String email, LocalDate questDate);

  @EntityGraph(attributePaths = {"mission", "member"})
  List<DailyQuest> findTop8ByMember_EmailOrderByQuestDateDesc(String email);

  List<DailyQuest> findByMember_EmailAndQuestDateBetweenOrderByQuestDateAsc(
      String email, LocalDate startDate, LocalDate endDate);

  @EntityGraph(attributePaths = {"mission", "member"})
  List<DailyQuest> findTop8ByStatusOrderByCompletedAtDesc(QuestStatus status);

  @EntityGraph(attributePaths = {"mission", "member"})
  Optional<DailyQuest> findWithDetailById(Long id);

  long countByMember_EmailAndStatus(String email, QuestStatus status);

  @Query("""
      select q.mission.category, count(q)
      from DailyQuest q
      where q.member.email = :email and q.status = com.example.ex76.entity.QuestStatus.COMPLETED
      group by q.mission.category
      order by count(q) desc
      """)
  List<Object[]> countCompletedByCategory(@Param("email") String email);
}
