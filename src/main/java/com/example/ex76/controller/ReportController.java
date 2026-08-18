package com.example.ex76.controller;

import com.example.ex76.dto.ReportForm;
import com.example.ex76.entity.*;
import com.example.ex76.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class ReportController {
  private final ReportService reportService;

  @GetMapping("/reports/new")
  public String form(@RequestParam(defaultValue = "REPORT") ReportKind kind,
                     @RequestParam(defaultValue = "GENERAL") ReportTargetType targetType,
                     @RequestParam(required = false) Long targetId, Model model) {
    if (!model.containsAttribute("form")) {
      ReportForm form = new ReportForm();
      form.setKind(kind); form.setTargetType(targetType); form.setTargetId(targetId);
      model.addAttribute("form", form);
    }
    model.addAttribute("kinds", ReportKind.values());
    return "report/form";
  }

  @PostMapping("/reports")
  public String submit(ReportForm form, Authentication auth, RedirectAttributes ra) {
    try {
      reportService.submit(auth.getName(), form);
      ra.addFlashAttribute("msg", "신고·문의가 운영자에게 전달되었습니다.");
      return "redirect:/community";
    } catch (RuntimeException e) {
      ra.addFlashAttribute("error", e.getMessage());
      ra.addFlashAttribute("form", form);
      return "redirect:/reports/new?kind=" + form.getKind()
          + "&targetType=" + form.getTargetType()
          + (form.getTargetId() == null ? "" : "&targetId=" + form.getTargetId());
    }
  }

  @GetMapping("/admin/reports")
  public String adminList(@RequestParam(required = false) ReportStatus status,
                          @RequestParam(defaultValue = "0") int page, Model model) {
    model.addAttribute("reports", reportService.getReports(status, page));
    model.addAttribute("statuses", ReportStatus.values());
    model.addAttribute("selectedStatus", status);
    return "report/admin-list";
  }

  @PostMapping("/admin/reports/{id}/review")
  public String review(@PathVariable Long id, @RequestParam ReportStatus status,
                       Authentication auth, RedirectAttributes ra) {
    try {
      reportService.review(id, status, auth.getName());
      ra.addFlashAttribute("msg", "처리 상태를 변경했습니다.");
    } catch (RuntimeException e) {
      ra.addFlashAttribute("error", e.getMessage());
    }
    return "redirect:/admin/reports";
  }
}
