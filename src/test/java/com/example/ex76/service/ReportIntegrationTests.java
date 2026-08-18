package com.example.ex76.service;

import com.example.ex76.dto.ReportForm;
import com.example.ex76.entity.*;
import com.example.ex76.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ReportIntegrationTests {
  @Autowired ReportService reportService;
  @Autowired UserReportRepository reportRepository;
  @Autowired ClubMemberRepository memberRepository;
  @Autowired BoardPostRepository postRepository;

  @Test
  @WithMockUser(username = "admin", roles = "ADMIN")
  void userCanReportPostAndAdminCanResolveIt() {
    ClubMember member = saveMember();
    BoardPost post = postRepository.save(BoardPost.builder()
        .author(member).category(BoardCategory.FREE).title("신고 대상").content("내용").build());
    ReportForm form = new ReportForm();
    form.setKind(ReportKind.REPORT);
    form.setTargetType(ReportTargetType.POST);
    form.setTargetId(post.getId());
    form.setReason("운영자 확인이 필요합니다.");

    Long id = reportService.submit(member.getEmail(), form);
    assertEquals(ReportStatus.PENDING, reportRepository.findById(id).orElseThrow().getStatus());

    reportService.review(id, ReportStatus.RESOLVED, "admin");
    assertEquals(ReportStatus.RESOLVED, reportRepository.findById(id).orElseThrow().getStatus());
  }

  private ClubMember saveMember() {
    ClubMember member = ClubMember.builder()
        .email("report-" + UUID.randomUUID()).password("encoded").name("신고자").build();
    member.addMemberRole(ClubMemberRole.USER);
    return memberRepository.save(member);
  }
}
