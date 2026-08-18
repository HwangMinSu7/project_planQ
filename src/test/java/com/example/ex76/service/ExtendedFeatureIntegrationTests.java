package com.example.ex76.service;

import com.example.ex76.entity.*;
import com.example.ex76.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.test.context.support.WithMockUser;

import com.example.ex76.dto.LikeResult;
import com.example.ex76.dto.MissionSuggestionForm;
import com.example.ex76.dto.MyPageDTO;

import java.time.LocalDate;
import java.time.DayOfWeek;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ExtendedFeatureIntegrationTests {
  @Autowired ClubMemberRepository memberRepository;
  @Autowired BoardPostRepository postRepository;
  @Autowired MissionRepository missionRepository;
  @Autowired DailyQuestRepository dailyQuestRepository;
  @Autowired BadgeRepository badgeRepository;
  @Autowired MemberBadgeRepository memberBadgeRepository;
  @Autowired BadgeService badgeService;
  @Autowired CommunityService communityService;
  @Autowired MissionSuggestionService suggestionService;
  @Autowired MissionSuggestionRepository suggestionRepository;
  @Autowired MyPageService myPageService;

  @Test
  void boardSearchFindsTitleAndContent() {
    ClubMember member = saveMember();
    String titleToken = "title-" + UUID.randomUUID();
    String contentToken = "content-" + UUID.randomUUID();
    postRepository.save(BoardPost.builder()
        .author(member).category(BoardCategory.FREE)
        .title(titleToken + " 산책 후기").content("저녁에 천천히 걸었어요").build());
    postRepository.save(BoardPost.builder()
        .author(member).category(BoardCategory.QUEST)
        .title("오늘의 사진").content(contentToken + " 사진을 찍었습니다").build());

    Page<BoardPost> byTitle = postRepository.search(null, titleToken, PageRequest.of(0, 10));
    Page<BoardPost> byContent = postRepository.search(BoardCategory.QUEST, contentToken, PageRequest.of(0, 10));

    assertEquals(1, byTitle.getTotalElements());
    assertEquals(1, byContent.getTotalElements());
    assertEquals(BoardCategory.QUEST, byContent.getContent().get(0).getCategory());
  }

  @Test
  void firstCompletedQuestAwardsFirstQuestBadge() {
    ClubMember member = saveMember();
    Mission mission = missionRepository.save(Mission.builder()
        .title("테스트 퀘스트").description("테스트 설명")
        .category(MissionCategory.DAILY).points(100).build());
    dailyQuestRepository.save(DailyQuest.builder()
        .member(member).mission(mission).questDate(LocalDate.now())
        .status(QuestStatus.COMPLETED).build());
    member.rewardQuest(LocalDate.now(), 100);

    List<Badge> newlyEarned = badgeService.evaluate(member.getEmail());

    assertTrue(newlyEarned.stream().anyMatch(badge -> badge.getCode() == BadgeCode.FIRST_QUEST));
    assertTrue(memberBadgeRepository.existsByMember_EmailAndBadge_Code(
        member.getEmail(), BadgeCode.FIRST_QUEST));
  }

  @Test
  void likeToggleAddsAndRemovesOnlyOneLike() {
    ClubMember member = saveMember();
    BoardPost post = postRepository.save(BoardPost.builder()
        .author(member).category(BoardCategory.FREE)
        .title("좋아요 테스트").content("좋아요 토글 테스트").build());

    LikeResult added = communityService.toggleLike(post.getId(), member.getEmail());
    LikeResult removed = communityService.toggleLike(post.getId(), member.getEmail());

    assertTrue(added.liked());
    assertEquals(1, added.likeCount());
    assertFalse(removed.liked());
    assertEquals(0, removed.likeCount());
  }

  @Test
  @WithMockUser(username = "admin@onequest.local", roles = "ADMIN")
  void adminApprovalOpensSuggestionForParticipants() {
    ClubMember member = saveMember();
    MissionSuggestionForm form = new MissionSuggestionForm();
    form.setTitle("승인 테스트 " + UUID.randomUUID());
    form.setDescription("관리자 승인 시 미션으로 생성되는지 확인합니다.");
    form.setCategory(MissionCategory.CREATIVE);
    Long suggestionId = suggestionService.suggest(member.getEmail(), form);
    long missionCountBefore = missionRepository.count();

    suggestionService.approve(suggestionId, "admin@onequest.local");

    MissionSuggestion approved = suggestionRepository.findById(suggestionId).orElseThrow();
    assertEquals(SuggestionStatus.APPROVED, approved.getStatus());
    assertEquals(missionCountBefore, missionRepository.count());
  }

  @Test
  void myPageShowsSevenDaysAndWeeklyCompletedCount() {
    ClubMember member = saveMember();
    Mission mission = missionRepository.save(Mission.builder()
        .title("주간 현황 테스트").description("월요일과 화요일 완료")
        .category(MissionCategory.DAILY).points(100).build());
    LocalDate monday = LocalDate.now().with(
        TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    dailyQuestRepository.save(DailyQuest.builder()
        .member(member).mission(mission).questDate(monday)
        .status(QuestStatus.COMPLETED).build());
    dailyQuestRepository.save(DailyQuest.builder()
        .member(member).mission(mission).questDate(monday.plusDays(1))
        .status(QuestStatus.COMPLETED).build());

    MyPageDTO myPage = myPageService.getMyPage(member.getEmail());

    assertEquals(7, myPage.weeklyQuests().size());
    assertEquals("월", myPage.weeklyQuests().get(0).dayLabel());
    assertEquals("일", myPage.weeklyQuests().get(6).dayLabel());
    assertEquals(2, myPage.weeklyCompletedCount());
    assertEquals(0, myPage.levelProgressPercent());
    assertEquals(500, myPage.pointsToNextLevel());
  }

  @Test
  void earnedBadgeCanBeSelectedAsFeaturedBadge() {
    ClubMember member = saveMember();
    Mission mission = missionRepository.save(Mission.builder()
        .title("대표 뱃지 테스트").description("첫 퀘스트 뱃지 획득")
        .category(MissionCategory.DAILY).points(100).build());
    dailyQuestRepository.save(DailyQuest.builder()
        .member(member).mission(mission).questDate(LocalDate.now())
        .status(QuestStatus.COMPLETED).build());
    badgeService.evaluate(member.getEmail());

    myPageService.updateFeaturedBadge(member.getEmail(), BadgeCode.FIRST_QUEST);
    MyPageDTO myPage = myPageService.getMyPage(member.getEmail());

    assertEquals(BadgeCode.FIRST_QUEST, member.getFeaturedBadgeCode());
    assertNotNull(myPage.featuredBadge());
    assertEquals("첫걸음", myPage.featuredBadge().getName());
    assertThrows(IllegalArgumentException.class,
        () -> myPageService.updateFeaturedBadge(member.getEmail(), BadgeCode.STREAK_7));
  }

  @Test
  void sixNewBadgeConditionsAwardAllNewBadges() {
    ClubMember member = saveMember();
    List<Mission> missions = new java.util.ArrayList<>();
    for (MissionCategory category : MissionCategory.values()) {
      missions.add(missionRepository.save(Mission.builder()
          .title("badge mission " + category.name())
          .description("badge condition test")
          .category(category).points(100).build()));
    }

    LocalDate monday = LocalDate.now().with(
        TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    for (int i = 0; i < 30; i++) {
      LocalDate questDate = i < 7
          ? monday.plusDays(i)
          : monday.minusDays(i - 6L);
      dailyQuestRepository.save(DailyQuest.builder()
          .member(member)
          .mission(missions.get(i % missions.size()))
          .questDate(questDate)
          .status(QuestStatus.COMPLETED)
          .build());
    }

    BoardPost likedPost = null;
    for (int i = 0; i < 5; i++) {
      BoardPost post = BoardPost.builder()
          .author(member).category(BoardCategory.FREE)
          .title("badge post " + i).content("badge post content").build();
      if (i == 0) {
        for (int like = 0; like < 10; like++) post.addLike();
        likedPost = post;
      }
      postRepository.save(post);
    }
    assertNotNull(likedPost);

    badgeService.evaluate(member.getEmail());

    assertEquals(BadgeCode.values().length, badgeRepository.count());
    for (BadgeCode code : List.of(
        BadgeCode.QUEST_10,
        BadgeCode.QUEST_30,
        BadgeCode.POST_5,
        BadgeCode.LIKE_10,
        BadgeCode.ALL_CATEGORY,
        BadgeCode.WEEK_CLEAR)) {
      assertTrue(memberBadgeRepository.existsByMember_EmailAndBadge_Code(
          member.getEmail(), code), code + " badge was not awarded");
    }
  }

  private ClubMember saveMember() {
    ClubMember member = ClubMember.builder()
        .email("feature-" + UUID.randomUUID() + "@test.local")
        .password("encoded").name("기능테스터").build();
    member.addMemberRole(ClubMemberRole.USER);
    return memberRepository.save(member);
  }
}
