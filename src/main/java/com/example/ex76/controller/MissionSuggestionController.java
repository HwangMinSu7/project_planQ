package com.example.ex76.controller;

import com.example.ex76.dto.MissionSuggestionForm;
import com.example.ex76.entity.MissionCategory;
import com.example.ex76.entity.SuggestionStatus;
import com.example.ex76.service.MissionSuggestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Duration;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/suggestions")
@RequiredArgsConstructor
public class MissionSuggestionController {
  private final MissionSuggestionService suggestionService;

  @GetMapping
  public String list(@RequestParam(required = false) SuggestionStatus status,
                     @RequestParam(defaultValue = "0") int page, Model model) {
    model.addAttribute("suggestions", suggestionService.getSuggestions(status, page));
    model.addAttribute("statuses", SuggestionStatus.values());
    model.addAttribute("selectedStatus", status);
    return "suggestion/list";
  }

  @GetMapping("/{id}")
  public String detail(@PathVariable Long id, Authentication auth, Model model) {
    var suggestion = suggestionService.getDetail(id);
    model.addAttribute("suggestion", suggestion);
    model.addAttribute("chatMessages", suggestionService.getComments(id));
    model.addAttribute("canChat", suggestionService.canChat(id, auth.getName()));
    long secondsLeft = suggestion.getAcceptRequestedAt() == null ? 0
        : Math.max(0, Duration.between(LocalDateTime.now(),
            suggestion.getAcceptRequestedAt().plusMinutes(10)).getSeconds());
    model.addAttribute("requestSecondsLeft", secondsLeft);
    return "suggestion/detail";
  }

  @GetMapping("/new")
  public String form(Authentication auth, Model model) {
    if (!model.containsAttribute("form")) model.addAttribute("form", new MissionSuggestionForm());
    model.addAttribute("categories", MissionCategory.values());
    model.addAttribute("memberPoints", suggestionService.getMemberPoints(auth.getName()));
    return "suggestion/form";
  }

  @PostMapping
  public String suggest(MissionSuggestionForm form, Authentication auth, RedirectAttributes ra) {
    try {
      suggestionService.suggest(auth.getName(), form);
      ra.addFlashAttribute("msg", "미션을 제안했습니다. 운영자가 승인하면 다른 이용자가 수행할 수 있습니다.");
      return "redirect:/suggestions";
    } catch (RuntimeException e) {
      ra.addFlashAttribute("error", e.getMessage());
      ra.addFlashAttribute("form", form);
      return "redirect:/suggestions/new";
    }
  }

  @PostMapping("/{id}/approve")
  public String approve(@PathVariable Long id, Authentication auth, RedirectAttributes ra) {
    runReview(() -> suggestionService.approve(id, auth.getName()), "제안을 승인해 수행자를 모집합니다.", ra);
    return "redirect:/suggestions";
  }

  @PostMapping("/{id}/reject")
  public String reject(@PathVariable Long id, Authentication auth, RedirectAttributes ra) {
    runReview(() -> suggestionService.reject(id, auth.getName()), "제안을 반려했습니다.", ra);
    return "redirect:/suggestions";
  }

  @PostMapping("/{id}/accept")
  public String accept(@PathVariable Long id, Authentication auth, RedirectAttributes ra) {
    runReview(() -> suggestionService.accept(id, auth.getName()), "작성자에게 미션 수락 요청을 보냈습니다.", ra);
    return "redirect:/suggestions/" + id;
  }

  @PostMapping("/{id}/accept/confirm")
  public String confirmAcceptance(@PathVariable Long id, Authentication auth, RedirectAttributes ra) {
    runReview(() -> suggestionService.confirmAcceptance(id, auth.getName()),
        "수행자를 확정했습니다. 실시간 채팅이 열렸습니다.", ra);
    return "redirect:/suggestions/" + id;
  }

  @PostMapping("/{id}/accept/decline")
  public String declineAcceptance(@PathVariable Long id, Authentication auth, RedirectAttributes ra) {
    runReview(() -> suggestionService.declineAcceptance(id, auth.getName()),
        "미션 수락 요청을 거절했습니다.", ra);
    return "redirect:/suggestions/" + id;
  }

  @PostMapping("/{id}/complete")
  public String complete(@PathVariable Long id, Authentication auth, RedirectAttributes ra) {
    runReview(() -> suggestionService.confirmCompletion(id, auth.getName()), "미션 완료 확인을 남겼습니다.", ra);
    return "redirect:/suggestions/" + id;
  }

  @PostMapping("/{id}/settle")
  public String settle(@PathVariable Long id, RedirectAttributes ra) {
    runReview(() -> suggestionService.settlePoints(id), "미션 완료와 보상 처리를 승인했습니다.", ra);
    return "redirect:/suggestions/" + id;
  }

  private void runReview(Runnable action, String message, RedirectAttributes ra) {
    try {
      action.run();
      ra.addFlashAttribute("msg", message);
    } catch (RuntimeException e) {
      ra.addFlashAttribute("error", e.getMessage());
    }
  }
}
