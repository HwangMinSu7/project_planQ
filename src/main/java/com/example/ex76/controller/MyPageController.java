package com.example.ex76.controller;

import com.example.ex76.service.BadgeService;
import com.example.ex76.service.MyPageService;
import com.example.ex76.entity.BadgeCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;

@Controller
@RequestMapping("/mypage")
@RequiredArgsConstructor
public class MyPageController {
  private final MyPageService myPageService;
  private final BadgeService badgeService;

  @GetMapping
  public String myPage(Authentication authentication, Model model) {
    badgeService.evaluate(authentication.getName());
    model.addAttribute("myPage", myPageService.getMyPage(authentication.getName()));
    return "mypage/index";
  }

  @PostMapping("/profile-image")
  public String updateProfileImage(Authentication authentication,
                                   @RequestParam MultipartFile profileImage,
                                   RedirectAttributes ra) {
    try {
      myPageService.updateProfileImage(authentication.getName(), profileImage);
      ra.addFlashAttribute("msg", "프로필 사진을 변경했습니다.");
    } catch (RuntimeException e) {
      ra.addFlashAttribute("error", e.getMessage());
    }
    return "redirect:/mypage";
  }

  @GetMapping("/profile-image")
  public ResponseEntity<?> profileImage(Authentication authentication) {
    var image = myPageService.getProfileImage(authentication.getName());
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(image.contentType()))
        .body(image.resource());
  }

  @PostMapping("/featured-badge")
  public String updateFeaturedBadge(Authentication authentication,
                                    @RequestParam BadgeCode badgeCode,
                                    RedirectAttributes ra) {
    try {
      myPageService.updateFeaturedBadge(authentication.getName(), badgeCode);
      ra.addFlashAttribute("msg", "대표 뱃지를 설정했습니다.");
    } catch (RuntimeException e) {
      ra.addFlashAttribute("error", e.getMessage());
    }
    return "redirect:/mypage";
  }
}
