package com.example.ex76.controller;

import com.example.ex76.dto.MemberDTO;
import com.example.ex76.service.MemberService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class UploadTestController {
  private final MemberService memberService;

  @GetMapping("/uploadEx")
  public void uploadEx() {  }

  @GetMapping("/login")
  public String login(HttpSession session) {
    MemberDTO memberDTO = (MemberDTO) session.getAttribute("loginMember");
    if (memberDTO != null) {return "redirect:/movie/list";}
    return "/member/login";
  }
  @PostMapping("/login")
  public String loginPost(String email, String password, HttpSession session, RedirectAttributes ra) {
    MemberDTO memberDTO = memberService.checkLogin(email, password);
    if(memberDTO != null) {
      session.setAttribute("loginMember", memberDTO);
      return "redirect:/movie/list";
    } else {
      ra.addFlashAttribute("msg", "Email or password is incorrect");
      return "redirect:/login";
    }
  }
}
// RedirectAttributes ra / ra.addFlashAttribute("msg", "Email or password is incorrect");
// ↑  모달창 코드