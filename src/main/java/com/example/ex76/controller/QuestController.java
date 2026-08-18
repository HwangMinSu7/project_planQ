package com.example.ex76.controller;

import com.example.ex76.dto.QuestCompletionResult;
import com.example.ex76.dto.QuestDashboardDTO;
import com.example.ex76.service.QuestImageStorage;
import com.example.ex76.service.QuestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/quest")
@RequiredArgsConstructor
public class QuestController {
  private final QuestService questService;

  @GetMapping
  public String dashboard(Authentication authentication, Model model) {
    QuestDashboardDTO dashboard = questService.getDashboard(authentication.getName());
    model.addAttribute("dashboard", dashboard);
    return "quest/dashboard";
  }

  @PostMapping("/reroll")
  public String reroll(Authentication authentication, RedirectAttributes ra) {
    try {
      questService.reroll(authentication.getName());
      ra.addFlashAttribute("msg", "새 퀘스트가 도착했습니다. 오늘의 교체 기회를 사용했어요.");
    } catch (RuntimeException e) {
      ra.addFlashAttribute("error", e.getMessage());
    }
    return "redirect:/quest";
  }

  @PostMapping("/complete")
  public String complete(Authentication authentication,
                         @RequestParam(required = false) String note,
                         @RequestParam(required = false) MultipartFile proofImage,
                         RedirectAttributes ra) {
    try {
      QuestCompletionResult result = questService.complete(authentication.getName(), note, proofImage);
      ra.addFlashAttribute("msg", "+" + result.earnedPoints() + "P 획득! "
          + result.streak() + "일 연속 달성, 현재 Lv." + result.level() + "입니다.");
    } catch (RuntimeException e) {
      ra.addFlashAttribute("error", e.getMessage());
    }
    return "redirect:/quest";
  }

  @GetMapping("/proof/{questId}")
  public ResponseEntity<?> proof(@PathVariable Long questId) {
    QuestImageStorage.StoredImage image = questService.getProofImage(questId);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename*=UTF-8''" +
            java.net.URLEncoder.encode(image.resource().getFilename(), StandardCharsets.UTF_8))
        .contentType(MediaType.parseMediaType(image.contentType()))
        .body(image.resource());
  }
}
