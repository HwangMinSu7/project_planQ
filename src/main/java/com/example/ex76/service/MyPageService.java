package com.example.ex76.service;

import com.example.ex76.dto.CategoryStatDTO;
import com.example.ex76.dto.MyPageDTO;
import com.example.ex76.dto.WeeklyQuestDTO;
import com.example.ex76.entity.*;
import com.example.ex76.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.time.DayOfWeek;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {
  private final ClubMemberRepository memberRepository;
  private final DailyQuestRepository dailyQuestRepository;
  private final BoardPostRepository postRepository;
  private final BoardCommentRepository commentRepository;
  private final MeetupParticipantRepository participantRepository;
  private final MemberBadgeRepository memberBadgeRepository;
  private final BadgeRepository badgeRepository;
  private final ProfileImageStorage profileImageStorage;

  public MyPageDTO getMyPage(String email) {
    ClubMember member = memberRepository.findById(email)
        .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
    List<CategoryStatDTO> categoryStats = dailyQuestRepository.countCompletedByCategory(email).stream()
        .map(row -> new CategoryStatDTO(((MissionCategory) row[0]).getLabel(), (Long) row[1]))
        .toList();
    List<WeeklyQuestDTO> weeklyQuests = getWeeklyQuests(email);

    List<MemberBadge> badges = memberBadgeRepository.findByMember_EmailOrderByEarnedAtDesc(email);
    Badge featuredBadge = member.getFeaturedBadgeCode() == null ? null
        : badgeRepository.findByCode(member.getFeaturedBadgeCode()).orElse(null);
    int levelProgressPoints = member.getRewardPoints() % 500;
    int levelProgressPercent = levelProgressPoints * 100 / 500;
    int pointsToNextLevel = 500 - levelProgressPoints;

    return new MyPageDTO(
        member,
        dailyQuestRepository.countByMember_EmailAndStatus(email, QuestStatus.COMPLETED),
        postRepository.countByAuthor_Email(email),
        commentRepository.countByAuthor_Email(email),
        participantRepository.countByMember_Email(email),
        levelProgressPercent,
        pointsToNextLevel,
        weeklyQuests,
        weeklyQuests.stream().filter(WeeklyQuestDTO::completed).count(),
        categoryStats,
        dailyQuestRepository.findTop8ByMember_EmailOrderByQuestDateDesc(email),
        postRepository.findTop5ByAuthor_EmailOrderByIdDesc(email),
        participantRepository.findTop5ByMember_EmailAndPost_MeetingAtAfterOrderByPost_MeetingAtAsc(
            email, LocalDateTime.now()),
        badges,
        featuredBadge);
  }

  @Transactional
  public void updateFeaturedBadge(String email, BadgeCode badgeCode) {
    ClubMember member = memberRepository.findById(email)
        .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
    if (badgeCode == null || !memberBadgeRepository
        .existsByMember_EmailAndBadge_Code(email, badgeCode)) {
      throw new IllegalArgumentException("획득한 뱃지만 대표 뱃지로 설정할 수 있습니다.");
    }
    member.changeFeaturedBadge(badgeCode);
  }

  private List<WeeklyQuestDTO> getWeeklyQuests(String email) {
    LocalDate today = LocalDate.now();
    LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    LocalDate sunday = monday.plusDays(6);
    Map<LocalDate, DailyQuest> questsByDate = dailyQuestRepository
        .findByMember_EmailAndQuestDateBetweenOrderByQuestDateAsc(email, monday, sunday)
        .stream().collect(Collectors.toMap(DailyQuest::getQuestDate, Function.identity()));

    return java.util.stream.IntStream.range(0, 7)
        .mapToObj(index -> {
          LocalDate date = monday.plusDays(index);
          DailyQuest quest = questsByDate.get(date);
          return new WeeklyQuestDTO(date, dayLabel(date.getDayOfWeek()),
              quest != null && quest.isCompleted(), date.equals(today));
        })
        .toList();
  }

  private String dayLabel(DayOfWeek dayOfWeek) {
    return switch (dayOfWeek) {
      case MONDAY -> "월";
      case TUESDAY -> "화";
      case WEDNESDAY -> "수";
      case THURSDAY -> "목";
      case FRIDAY -> "금";
      case SATURDAY -> "토";
      case SUNDAY -> "일";
    };
  }

  @Transactional
  public void updateProfileImage(String email, MultipartFile image) {
    ClubMember member = memberRepository.findById(email)
        .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
    String oldPath = member.getProfileImagePath();
    String newPath = profileImageStorage.save(image);
    member.changeProfileImage(newPath);
    profileImageStorage.delete(oldPath);
  }

  public ProfileImageStorage.StoredImage getProfileImage(String email) {
    ClubMember member = memberRepository.findById(email)
        .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
    return profileImageStorage.load(member.getProfileImagePath());
  }
}
