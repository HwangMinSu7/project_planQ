package com.example.ex76.controller;

import com.example.ex76.dto.ClubMemberDTO;
import com.example.ex76.service.ClubMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
  private final ClubMemberService clubMemberService;

  @GetMapping({"login", "logout", "accessDenied", "authenticationFailure"})
  public void auth() {}

  @GetMapping("modify")
  public String modifyForm(Authentication authentication, Model model) {
    model.addAttribute("member", clubMemberService.getProfile(authentication.getName()));
    return "auth/modify";
  }

  @GetMapping("register")
  public void register() {}

  @PostMapping("register")
  public String register(ClubMemberDTO dto, RedirectAttributes ra) {
    try {
      String username = clubMemberService.register(dto);
      ra.addFlashAttribute("msg", username + " 계정이 만들어졌습니다. 로그인해 주세요.");
      return "redirect:/auth/login";
    } catch (IllegalArgumentException e) {
      ra.addFlashAttribute("error", e.getMessage());
      ra.addFlashAttribute("form", dto);
      return "redirect:/auth/register";
    }
  }

  @PostMapping("modify")
  public String modify(ClubMemberDTO dto, Authentication authentication, RedirectAttributes ra) {
    try {
      String username = clubMemberService.modify(authentication.getName(), dto);
      ra.addFlashAttribute("msg", username + "의 정보가 변경되었습니다.");
      return "redirect:/mypage";
    } catch (IllegalArgumentException e) {
      ra.addFlashAttribute("error", e.getMessage());
      return "redirect:/auth/modify";
    }
  }
}
