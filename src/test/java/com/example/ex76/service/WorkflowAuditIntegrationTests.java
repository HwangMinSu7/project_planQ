package com.example.ex76.service;

import com.example.ex76.dto.BoardPostForm;
import com.example.ex76.dto.ClubMemberDTO;
import com.example.ex76.dto.QuestCompletionResult;
import com.example.ex76.dto.QuestDashboardDTO;
import com.example.ex76.entity.*;
import com.example.ex76.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class WorkflowAuditIntegrationTests {
  @Autowired ClubMemberRepository memberRepository;
  @Autowired BoardPostRepository postRepository;
  @Autowired MissionSuggestionRepository suggestionRepository;
  @Autowired MemberBadgeRepository memberBadgeRepository;
  @Autowired QuestService questService;
  @Autowired CommunityService communityService;
  @Autowired MissionSuggestionService suggestionService;
  @Autowired ClubMemberService clubMemberService;

  @Test
  void freshMemberCanRerollOnceCompleteAndEarnBadge() {
    ClubMember member = saveMember("quest-audit");
    QuestDashboardDTO first = questService.getDashboard(member.getEmail());
    Long firstMissionId = first.today().getMission().getId();

    questService.reroll(member.getEmail());
    QuestDashboardDTO rerolled = questService.getDashboard(member.getEmail());

    assertNotEquals(firstMissionId, rerolled.today().getMission().getId());
    assertTrue(rerolled.today().isRerolled());
    assertThrows(IllegalStateException.class, () -> questService.reroll(member.getEmail()));

    QuestCompletionResult completed = questService.complete(member.getEmail(), "완료 기록", null);
    assertEquals(100, completed.earnedPoints());
    assertEquals(100, completed.totalPoints());
    assertEquals(1, completed.streak());
    assertTrue(memberBadgeRepository.existsByMember_EmailAndBadge_Code(
        member.getEmail(), BadgeCode.FIRST_QUEST));
    assertThrows(IllegalStateException.class,
        () -> questService.complete(member.getEmail(), "중복 완료", null));
  }

  @Test
  void meetupCapacityIsEnforcedAndSeatReturnsAfterLeaving() {
    ClubMember owner = saveMember("owner");
    ClubMember first = saveMember("first");
    ClubMember second = saveMember("second");
    ClubMember waiting = saveMember("waiting");
    BoardPostForm form = meetupForm(2);
    Long postId = communityService.create(owner.getEmail(), form);

    communityService.join(postId, first.getEmail());
    communityService.join(postId, second.getEmail());
    assertThrows(IllegalStateException.class,
        () -> communityService.join(postId, waiting.getEmail()));
    assertEquals(2, postRepository.findById(postId).orElseThrow().getParticipantCount());

    communityService.leave(postId, first.getEmail());
    communityService.join(postId, waiting.getEmail());
    assertEquals(2, postRepository.findById(postId).orElseThrow().getParticipantCount());
  }

  @Test
  void anotherMemberCannotEditOrDeletePostThroughService() {
    ClubMember owner = saveMember("post-owner");
    ClubMember attacker = saveMember("post-attacker");
    BoardPostForm form = new BoardPostForm();
    form.setCategory(BoardCategory.FREE);
    form.setTitle("소유권 테스트");
    form.setContent("작성자만 변경 가능");
    Long postId = communityService.create(owner.getEmail(), form);

    assertThrows(IllegalStateException.class,
        () -> communityService.getEditForm(postId, attacker.getEmail()));
    assertThrows(IllegalStateException.class,
        () -> communityService.delete(postId, attacker.getEmail()));
  }

  @Test
  @WithMockUser(username = "normal@test.local", roles = "USER")
  void normalMemberCannotApproveMissionSuggestion() {
    ClubMember author = saveMember("suggestion-author");
    MissionSuggestion suggestion = suggestionRepository.save(MissionSuggestion.builder()
        .author(author).title("권한 테스트").description("관리자만 승인")
        .category(MissionCategory.DAILY).build());

    assertThrows(AccessDeniedException.class,
        () -> suggestionService.approve(suggestion.getId(), "normal@test.local"));
    assertEquals(SuggestionStatus.PENDING, suggestion.getStatus());
  }

  @Test
  void memberMustNotModifyAnotherMembersProfile() {
    ClubMember attacker = saveMember("attacker");
    ClubMember victim = saveMember("victim");
    ClubMemberDTO dto = ClubMemberDTO.builder()
        .email(victim.getEmail()).name("변조된 이름").password("new-password")
        .fromSocial(false).build();

    String modifiedEmail = clubMemberService.modify(attacker.getEmail(), dto);

    assertEquals(attacker.getEmail(), modifiedEmail);
    assertEquals("변조된 이름", memberRepository.findById(attacker.getEmail()).orElseThrow().getName());
    assertEquals("victim", memberRepository.findById(victim.getEmail()).orElseThrow().getName());
  }

  private ClubMember saveMember(String prefix) {
    ClubMember member = ClubMember.builder()
        .email(prefix + "-" + UUID.randomUUID() + "@audit.local")
        .password("encoded").name(prefix).build();
    member.addMemberRole(ClubMemberRole.USER);
    return memberRepository.save(member);
  }

  private BoardPostForm meetupForm(int capacity) {
    BoardPostForm form = new BoardPostForm();
    form.setCategory(BoardCategory.MEETUP);
    form.setTitle("정원 테스트 모임");
    form.setContent("정원과 취소 연결 테스트");
    form.setMeetingAt(LocalDateTime.now().plusDays(2));
    form.setMeetingPlace("테스트 장소");
    form.setMaxParticipants(capacity);
    return form;
  }
}
