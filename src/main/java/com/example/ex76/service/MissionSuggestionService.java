package com.example.ex76.service;

import com.example.ex76.dto.MissionSuggestionForm;
import com.example.ex76.dto.MissionChatMessageDTO;
import com.example.ex76.entity.*;
import com.example.ex76.repository.ClubMemberRepository;
import com.example.ex76.repository.MissionRepository;
import com.example.ex76.repository.MissionSuggestionRepository;
import com.example.ex76.repository.MissionSuggestionCommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MissionSuggestionService {
  private final MissionSuggestionRepository suggestionRepository;
  private final ClubMemberRepository memberRepository;
  private final MissionSuggestionCommentRepository commentRepository;
  private final NotificationService notificationService;

  @Transactional(readOnly = true)
  public Page<MissionSuggestion> getSuggestions(SuggestionStatus status, int page) {
    PageRequest pageable = PageRequest.of(Math.max(page, 0), 10);
    return status == null
        ? suggestionRepository.findAllByOrderByIdDesc(pageable)
        : suggestionRepository.findByStatusOrderByIdDesc(status, pageable);
  }

  @Transactional(readOnly = true)
  public int getMemberPoints(String email) {
    return memberRepository.findById(email)
        .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."))
        .getRewardPoints();
  }

  public MissionSuggestion getDetail(Long id) {
    MissionSuggestion suggestion = suggestionRepository.findDetailById(id)
        .orElseThrow(() -> new IllegalArgumentException("미션 제안을 찾을 수 없습니다."));
    if (suggestion.getPerformer() == null && suggestion.getApplicant() != null
        && suggestion.getAcceptRequestedAt() != null
        && !suggestion.getAcceptRequestedAt().plusMinutes(10).isAfter(java.time.LocalDateTime.now())) {
      expireOne(suggestion);
    }
    return suggestion;
  }

  public Long suggest(String email, MissionSuggestionForm form) {
    validate(form);
    ClubMember author = memberRepository.findById(email)
        .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
    author.spendPoints(form.getBountyPoints());
    MissionSuggestion suggestion = MissionSuggestion.builder()
        .author(author)
        .title(form.getTitle().trim())
        .description(form.getDescription().trim())
        .category(form.getCategory())
        .bountyPoints(form.getBountyPoints())
        .build();
    return suggestionRepository.save(suggestion).getId();
  }

  @PreAuthorize("hasRole('ADMIN')")
  public void approve(Long suggestionId, String reviewerEmail) {
    MissionSuggestion suggestion = getSuggestion(suggestionId);
    suggestion.approve(reviewerEmail);
  }

  @PreAuthorize("hasRole('ADMIN')")
  public void reject(Long suggestionId, String reviewerEmail) {
    MissionSuggestion suggestion = getSuggestion(suggestionId);
    suggestion.reject(reviewerEmail);
    if (suggestion.getBountyPoints() > 0) {
      suggestion.getAuthor().addPoints(suggestion.getBountyPoints());
    }
  }

  public void accept(Long suggestionId, String email) {
    MissionSuggestion suggestion = getSuggestion(suggestionId);
    ClubMember applicant = memberRepository.findById(email)
        .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
    suggestion.requestAcceptance(applicant);
    notificationService.send(suggestion.getAuthor(),
        applicant.getName() + "님이 '" + suggestion.getTitle() + "' 미션 수락을 요청했습니다. 10분 안에 확인해 주세요.",
        "/suggestions/" + suggestionId);
  }

  public void confirmAcceptance(Long suggestionId, String email) {
    MissionSuggestion suggestion = getSuggestion(suggestionId);
    if (suggestion.getAcceptRequestedAt() != null
        && !suggestion.getAcceptRequestedAt().plusMinutes(10).isAfter(java.time.LocalDateTime.now())) {
      expireOne(suggestion);
      throw new IllegalStateException("수락 요청 시간이 지나 자동으로 취소되었습니다.");
    }
    ClubMember performer = suggestion.confirmAcceptance(email);
    notificationService.send(performer,
        "'" + suggestion.getTitle() + "' 미션 수락이 확정되었습니다. 이제 실시간 채팅을 이용할 수 있습니다.",
        "/suggestions/" + suggestionId);
  }

  public void declineAcceptance(Long suggestionId, String email) {
    MissionSuggestion suggestion = getSuggestion(suggestionId);
    ClubMember applicant = suggestion.cancelAcceptanceRequest(email);
    if (applicant == null) throw new IllegalStateException("취소할 수락 요청이 없습니다.");
    notificationService.send(applicant,
        "'" + suggestion.getTitle() + "' 미션 수락 요청이 작성자에 의해 거절되었습니다.",
        "/suggestions/" + suggestionId);
  }

  public void confirmCompletion(Long suggestionId, String email) {
    MissionSuggestion suggestion = getSuggestion(suggestionId);
    suggestion.confirmCompletion(email);
    ClubMember recipient = suggestion.getAuthor().getEmail().equals(email)
        ? suggestion.getPerformer() : suggestion.getAuthor();
    notificationService.send(recipient,
        "'" + suggestion.getTitle() + "' 미션의 상대방이 완료 확인을 눌렀습니다.",
        "/suggestions/" + suggestionId);
  }

  @PreAuthorize("hasRole('ADMIN')")
  public void settlePoints(Long suggestionId) {
    MissionSuggestion suggestion = getSuggestion(suggestionId);
    suggestion.settle();
    if (suggestion.getBountyPoints() > 0) {
      suggestion.getPerformer().addPoints(suggestion.getBountyPoints());
    }
  }

  @Scheduled(fixedDelay = 60_000)
  public void expirePendingAcceptances() {
    expirePendingAcceptances(java.time.LocalDateTime.now());
  }

  public int expirePendingAcceptances(java.time.LocalDateTime now) {
    var expired = suggestionRepository
        .findByPerformerIsNullAndApplicantIsNotNullAndAcceptRequestedAtBefore(now.minusMinutes(10));
    expired.forEach(this::expireOne);
    return expired.size();
  }

  @Transactional(readOnly = true)
  public java.util.List<MissionSuggestionComment> getComments(Long suggestionId) {
    return commentRepository.findBySuggestion_IdOrderByIdAsc(suggestionId);
  }

  @Transactional(readOnly = true)
  public boolean canChat(Long suggestionId, String email) {
    MissionSuggestion suggestion = getSuggestion(suggestionId);
    ClubMember member = memberRepository.findById(email)
        .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
    return suggestion.getPerformer() != null && (suggestion.getAuthor().getEmail().equals(email)
        || suggestion.getPerformer().getEmail().equals(email)
        || member.getRoleSet().contains(ClubMemberRole.ADMIN));
  }

  public MissionChatMessageDTO addChatMessage(Long suggestionId, String email, String content) {
    if (!canChat(suggestionId, email)) {
      throw new IllegalStateException("확정된 미션 작성자와 수행자만 채팅할 수 있습니다.");
    }
    if (content == null || content.isBlank()) throw new IllegalArgumentException("메시지를 입력해 주세요.");
    if (content.trim().length() > 600) throw new IllegalArgumentException("메시지는 600자 이하로 입력해 주세요.");
    MissionSuggestion suggestion = getSuggestion(suggestionId);
    ClubMember author = memberRepository.findById(email).orElseThrow();
    MissionSuggestionComment saved = commentRepository.saveAndFlush(MissionSuggestionComment.builder()
        .suggestion(suggestion).author(author).content(content.trim()).build());
    ClubMember recipient = suggestion.getAuthor().getEmail().equals(email)
        ? suggestion.getPerformer() : suggestion.getAuthor();
    if (recipient != null && !recipient.getEmail().equals(email)) {
      notificationService.send(recipient,
          "'" + suggestion.getTitle() + "' 미션에 새 채팅이 도착했습니다.",
          "/suggestions/" + suggestionId);
    }
    return new MissionChatMessageDTO(saved.getId(), author.getEmail(), author.getName(),
        saved.getContent(), saved.getRegDate());
  }

  private void expireOne(MissionSuggestion suggestion) {
    ClubMember applicant = suggestion.expireAcceptanceRequest();
    if (applicant != null) {
      notificationService.send(applicant,
          "'" + suggestion.getTitle() + "' 미션 수락 요청이 10분 안에 확인되지 않아 자동 취소되었습니다.",
          "/suggestions/" + suggestion.getId());
    }
  }

  private MissionSuggestion getSuggestion(Long id) {
    return suggestionRepository.findDetailById(id)
        .orElseThrow(() -> new IllegalArgumentException("미션 제안을 찾을 수 없습니다."));
  }

  private void validate(MissionSuggestionForm form) {
    if (form.getTitle() == null || form.getTitle().isBlank()) {
      throw new IllegalArgumentException("미션 제목을 입력해 주세요.");
    }
    if (form.getDescription() == null || form.getDescription().isBlank()) {
      throw new IllegalArgumentException("미션 설명을 입력해 주세요.");
    }
    if (form.getTitle().trim().length() > 120) {
      throw new IllegalArgumentException("미션 제목은 120자 이하로 입력해 주세요.");
    }
    if (form.getDescription().trim().length() > 500) {
      throw new IllegalArgumentException("미션 설명은 500자 이하로 입력해 주세요.");
    }
    if (form.getCategory() == null) throw new IllegalArgumentException("카테고리를 선택해 주세요.");
    if (form.getBountyPoints() < 0 || form.getBountyPoints() > 10000
        || form.getBountyPoints() % 10 != 0) {
      throw new IllegalArgumentException("보상 포인트는 0~10000P 사이에서 10P 단위로 입력해 주세요.");
    }
  }
}
