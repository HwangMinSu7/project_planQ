package com.example.ex76.service;

import com.example.ex76.entity.*;
import com.example.ex76.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BadgeService {
  private final BadgeRepository badgeRepository;
  private final MemberBadgeRepository memberBadgeRepository;
  private final ClubMemberRepository memberRepository;
  private final DailyQuestRepository dailyQuestRepository;
  private final BoardCommentRepository commentRepository;
  private final BoardPostRepository postRepository;
  private final MeetupParticipantRepository participantRepository;

  public List<Badge> evaluate(String email) {
    ClubMember member = memberRepository.findById(email)
        .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
    long completed = dailyQuestRepository.countByMember_EmailAndStatus(email, QuestStatus.COMPLETED);
    long comments = commentRepository.countByAuthor_Email(email);
    long posts = postRepository.countByAuthor_Email(email);
    long receivedLikes = postRepository.sumLikeCountByAuthorEmail(email);
    long meetupsCreated = postRepository.countByAuthor_EmailAndCategory(email, BoardCategory.MEETUP);
    long meetupsJoined = participantRepository.countByMember_Email(email);
    long completedCategories = dailyQuestRepository.countCompletedByCategory(email).size();
    LocalDate monday = LocalDate.now().with(
        TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    long weeklyCompleted = dailyQuestRepository
        .findByMember_EmailAndQuestDateBetweenOrderByQuestDateAsc(email, monday, monday.plusDays(6))
        .stream().filter(DailyQuest::isCompleted).count();

    List<Badge> earned = new ArrayList<>();
    awardIf(member, BadgeCode.FIRST_QUEST, completed >= 1, earned);
    awardIf(member, BadgeCode.STREAK_3, member.getCurrentStreak() >= 3, earned);
    awardIf(member, BadgeCode.STREAK_7, member.getCurrentStreak() >= 7, earned);
    awardIf(member, BadgeCode.COMMENT_10, comments >= 10, earned);
    awardIf(member, BadgeCode.FIRST_MEETUP, meetupsCreated >= 1, earned);
    awardIf(member, BadgeCode.JOIN_3, meetupsJoined >= 3, earned);
    awardIf(member, BadgeCode.QUEST_10, completed >= 10, earned);
    awardIf(member, BadgeCode.QUEST_30, completed >= 30, earned);
    awardIf(member, BadgeCode.POST_5, posts >= 5, earned);
    awardIf(member, BadgeCode.LIKE_10, receivedLikes >= 10, earned);
    awardIf(member, BadgeCode.ALL_CATEGORY,
        completedCategories >= MissionCategory.values().length, earned);
    awardIf(member, BadgeCode.WEEK_CLEAR, weeklyCompleted >= 7, earned);
    return earned;
  }

  private void awardIf(ClubMember member, BadgeCode code, boolean condition, List<Badge> earned) {
    if (!condition || memberBadgeRepository.existsByMember_EmailAndBadge_Code(member.getEmail(), code)) return;
    Badge badge = badgeRepository.findByCode(code)
        .orElseThrow(() -> new IllegalStateException("뱃지 초기 데이터가 없습니다: " + code));
    memberBadgeRepository.save(MemberBadge.builder()
        .member(member).badge(badge).earnedAt(LocalDateTime.now()).build());
    earned.add(badge);
  }
}
