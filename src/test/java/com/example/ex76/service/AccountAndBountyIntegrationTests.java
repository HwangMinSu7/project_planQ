package com.example.ex76.service;

import com.example.ex76.dto.ClubMemberDTO;
import com.example.ex76.dto.MissionSuggestionForm;
import com.example.ex76.entity.*;
import com.example.ex76.repository.ClubMemberRepository;
import com.example.ex76.repository.MissionSuggestionRepository;
import com.example.ex76.repository.UserNotificationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AccountAndBountyIntegrationTests {
  @Autowired ClubMemberService memberService;
  @Autowired ClubMemberRepository memberRepository;
  @Autowired PasswordEncoder passwordEncoder;
  @Autowired MissionSuggestionService suggestionService;
  @Autowired MissionSuggestionRepository suggestionRepository;
  @Autowired QuestService questService;
  @Autowired UserDetailsService userDetailsService;
  @Autowired UserNotificationRepository notificationRepository;

  @Test
  void registrationUsesUsernameAndPasswordConfirmation() {
    String username = "Student1" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    ClubMemberDTO mismatch = ClubMemberDTO.builder()
        .username(username).password("1234").passwordConfirm("9999").name("학생").build();
    assertThrows(IllegalArgumentException.class, () -> memberService.register(mismatch));

    ClubMemberDTO valid = ClubMemberDTO.builder()
        .username(username).password("1234").passwordConfirm("1234").name("학생").build();
    assertEquals(username.toLowerCase(), memberService.register(valid));
    assertTrue(memberRepository.existsById(username.toLowerCase()));
    assertEquals(username.toLowerCase(),
        userDetailsService.loadUserByUsername(username.toUpperCase()).getUsername());

    String emailStyleId = "plan1" + UUID.randomUUID().toString().replace("-", "").substring(0, 5)
        + "@test.com";
    ClubMemberDTO emailStyle = ClubMemberDTO.builder()
        .username(emailStyleId).password("1234").passwordConfirm("1234").name("이메일형 아이디").build();
    assertEquals(emailStyleId, memberService.register(emailStyle));

    ClubMemberDTO noNumber = ClubMemberDTO.builder()
        .username("onlyletters").password("1234").passwordConfirm("1234").name("잘못된 아이디").build();
    assertThrows(IllegalArgumentException.class, () -> memberService.register(noNumber));
  }

  @Test
  void passwordChangeChecksCurrentAndNewConfirmation() {
    ClubMember member = saveMember("password-user", 0);
    ClubMemberDTO wrongCurrent = ClubMemberDTO.builder()
        .name(member.getName()).currentPassword("wrong")
        .newPassword("new-pass").newPasswordConfirm("new-pass").build();
    assertThrows(IllegalArgumentException.class,
        () -> memberService.modify(member.getEmail(), wrongCurrent));

    ClubMemberDTO mismatch = ClubMemberDTO.builder()
        .name(member.getName()).currentPassword("1234")
        .newPassword("new-pass").newPasswordConfirm("different").build();
    assertThrows(IllegalArgumentException.class,
        () -> memberService.modify(member.getEmail(), mismatch));

    ClubMemberDTO valid = ClubMemberDTO.builder()
        .name(member.getName()).currentPassword("1234")
        .newPassword("new-pass").newPasswordConfirm("new-pass").build();
    memberService.modify(member.getEmail(), valid);
    assertTrue(passwordEncoder.matches("new-pass", member.getPassword()));
  }

  @Test
  @WithMockUser(username = "admin", roles = "ADMIN")
  void bountyPointsMoveOnlyAfterBothConfirmAndAdminSettles() {
    ClubMember author = saveMember("bounty-author", 100);
    ClubMember performer = saveMember("bounty-performer", 0);
    MissionSuggestionForm form = new MissionSuggestionForm();
    form.setTitle("컴퓨터 설정 도와주기");
    form.setDescription("간단한 프로그램 설정을 도와주세요.");
    form.setCategory(MissionCategory.SOCIAL);
    form.setBountyPoints(100);

    Long id = suggestionService.suggest(author.getEmail(), form);
    assertEquals(0, author.getRewardPoints());
    suggestionService.approve(id, "admin");
    suggestionService.accept(id, performer.getEmail());
    assertEquals(performer.getEmail(), suggestionService.getDetail(id).getApplicant().getEmail());
    assertTrue(notificationRepository.countByRecipient_EmailAndReadFalse(author.getEmail()) > 0);
    suggestionService.confirmAcceptance(id, author.getEmail());
    suggestionService.confirmCompletion(id, author.getEmail());
    assertThrows(IllegalStateException.class, () -> suggestionService.settlePoints(id));

    suggestionService.confirmCompletion(id, performer.getEmail());
    suggestionService.settlePoints(id);

    MissionSuggestion suggestion = suggestionRepository.findById(id).orElseThrow();
    assertTrue(suggestion.isPointsSettled());
    assertEquals(100, performer.getRewardPoints());
    assertThrows(IllegalStateException.class, () -> suggestionService.settlePoints(id));
  }

  @Test
  void mainQuestUsesOnlyWebFriendlyMission() {
    ClubMember member = saveMember("web-quest-user", 0);
    assertTrue(questService.getDashboard(member.getEmail()).today().getMission().isWebFriendly());
  }

  @Test
  @WithMockUser(username = "admin", roles = "ADMIN")
  void approvedZeroPointSuggestionCanStillBeAccepted() {
    ClubMember author = saveMember("zero-author", 0);
    ClubMember performer = saveMember("zero-performer", 0);
    MissionSuggestionForm form = new MissionSuggestionForm();
    form.setTitle("보상 없이 함께하는 미션");
    form.setDescription("포인트가 없어도 다른 이용자가 참여할 수 있습니다.");
    form.setCategory(MissionCategory.SOCIAL);
    form.setBountyPoints(0);

    Long id = suggestionService.suggest(author.getEmail(), form);
    suggestionService.approve(id, "admin");
    suggestionService.accept(id, performer.getEmail());
    suggestionService.confirmAcceptance(id, author.getEmail());

    MissionSuggestion suggestion = suggestionService.getDetail(id);
    assertEquals(performer.getEmail(), suggestion.getPerformer().getEmail());
  }

  @Test
  @WithMockUser(username = "admin", roles = "ADMIN")
  void unconfirmedAcceptanceExpiresAfterTenMinutes() {
    ClubMember author = saveMember("timeout-author", 0);
    ClubMember applicant = saveMember("timeout-applicant", 0);
    MissionSuggestionForm form = new MissionSuggestionForm();
    form.setTitle("10분 수락 확인 테스트");
    form.setDescription("작성자가 확인하지 않으면 자동 취소됩니다.");
    form.setCategory(MissionCategory.SOCIAL);
    Long id = suggestionService.suggest(author.getEmail(), form);
    suggestionService.approve(id, "admin");
    suggestionService.accept(id, applicant.getEmail());

    assertEquals(1, suggestionService.expirePendingAcceptances(java.time.LocalDateTime.now().plusMinutes(11)));
    MissionSuggestion expired = suggestionService.getDetail(id);
    assertNull(expired.getApplicant());
    assertNull(expired.getPerformer());
    assertTrue(notificationRepository.countByRecipient_EmailAndReadFalse(applicant.getEmail()) > 0);
  }

  @Test
  @WithMockUser(username = "admin", roles = "ADMIN")
  void confirmedParticipantsCanSaveChatMessages() {
    ClubMember author = saveMember("chat-author", 0);
    ClubMember performer = saveMember("chat-performer", 0);
    MissionSuggestionForm form = new MissionSuggestionForm();
    form.setTitle("실시간 채팅 테스트");
    form.setDescription("확정된 두 회원만 채팅합니다.");
    form.setCategory(MissionCategory.SOCIAL);
    Long id = suggestionService.suggest(author.getEmail(), form);
    suggestionService.approve(id, "admin");
    suggestionService.accept(id, performer.getEmail());
    assertFalse(suggestionService.canChat(id, performer.getEmail()));
    suggestionService.confirmAcceptance(id, author.getEmail());
    assertTrue(suggestionService.canChat(id, performer.getEmail()));
    assertTrue(suggestionService.canChat(id, author.getEmail()));

    var message = suggestionService.addChatMessage(id, performer.getEmail(), "안녕하세요!");

    assertEquals("안녕하세요!", message.content());
    assertNotNull(message.sentAt());
    assertEquals(1, suggestionService.getComments(id).size());
    assertTrue(notificationRepository.countByRecipient_EmailAndReadFalse(author.getEmail()) > 0);
  }

  private ClubMember saveMember(String prefix, int points) {
    ClubMember member = ClubMember.builder()
        .email(prefix + "-" + UUID.randomUUID()).password(passwordEncoder.encode("1234"))
        .name(prefix).build();
    member.addMemberRole(ClubMemberRole.USER);
    if (points > 0) member.addPoints(points);
    return memberRepository.save(member);
  }
}
