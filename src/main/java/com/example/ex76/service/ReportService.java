package com.example.ex76.service;

import com.example.ex76.dto.ReportForm;
import com.example.ex76.entity.*;
import com.example.ex76.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportService {
  private final UserReportRepository reportRepository;
  private final ClubMemberRepository memberRepository;
  private final BoardPostRepository postRepository;
  private final BoardCommentRepository commentRepository;
  private final MissionSuggestionRepository suggestionRepository;

  public Long submit(String email, ReportForm form) {
    if (form.getKind() == null || form.getTargetType() == null) {
      throw new IllegalArgumentException("신고 또는 문의 종류를 선택해 주세요.");
    }
    if (form.getReason() == null || form.getReason().isBlank()) {
      throw new IllegalArgumentException("내용을 입력해 주세요.");
    }
    if (form.getReason().trim().length() > 1000) {
      throw new IllegalArgumentException("내용은 1000자 이하로 입력해 주세요.");
    }
    ClubMember reporter = memberRepository.findById(email)
        .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
    String targetTitle = resolveTargetTitle(form.getTargetType(), form.getTargetId());
    UserReport report = UserReport.builder()
        .reporter(reporter).kind(form.getKind()).targetType(form.getTargetType())
        .targetId(form.getTargetId()).targetTitle(targetTitle)
        .reason(form.getReason().trim()).build();
    return reportRepository.save(report).getId();
  }

  @PreAuthorize("hasRole('ADMIN')")
  @Transactional(readOnly = true)
  public Page<UserReport> getReports(ReportStatus status, int page) {
    PageRequest pageable = PageRequest.of(Math.max(page, 0), 15);
    return status == null ? reportRepository.findAllByOrderByIdDesc(pageable)
        : reportRepository.findByStatusOrderByIdDesc(status, pageable);
  }

  @PreAuthorize("hasRole('ADMIN')")
  public void review(Long id, ReportStatus status, String reviewerEmail) {
    reportRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("신고·문의를 찾을 수 없습니다."))
        .review(status, reviewerEmail);
  }

  private String resolveTargetTitle(ReportTargetType type, Long targetId) {
    if (type == ReportTargetType.GENERAL) return "일반 문의";
    if (targetId == null) throw new IllegalArgumentException("신고 대상을 찾을 수 없습니다.");
    return switch (type) {
      case POST -> "게시글: " + postRepository.findById(targetId)
          .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다.")).getTitle();
      case COMMENT -> "댓글: " + shorten(commentRepository.findById(targetId)
          .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다.")).getContent());
      case SUGGESTION -> "미션 제안: " + suggestionRepository.findById(targetId)
          .orElseThrow(() -> new IllegalArgumentException("미션 제안을 찾을 수 없습니다.")).getTitle();
      case GENERAL -> "일반 문의";
    };
  }

  private String shorten(String value) {
    return value.length() <= 120 ? value : value.substring(0, 120) + "...";
  }
}
