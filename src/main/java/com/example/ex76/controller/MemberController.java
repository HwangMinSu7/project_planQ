package com.example.ex76.controller;

import com.example.ex76.dto.MemberDTO;
import com.example.ex76.dto.PageRequestDTO;
import com.example.ex76.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@Log4j2
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {
  private final MemberService memberService;

  @GetMapping({"", "/", "/list"})
  public String list(Model model, PageRequestDTO pageRequestDTO) {
    model.addAttribute("pageResultDTO", memberService.getList(pageRequestDTO));
    return "/member/list";
  }

  @GetMapping("register")
  public void register() {}

  @PostMapping("register")
  public String register(RedirectAttributes ra, MemberDTO memberDTO) {
    Long mid = memberService.register(memberDTO);
    ra.addFlashAttribute("msg", mid + "번 회원이 등록 되었습니다.");
    return "redirect:/member/list";
  }

  @GetMapping({"read, modify"})
  public void read(Model model, PageRequestDTO pageRequestDTO, Long mid) {
    model.addAttribute("memberDTO", memberService.read(mid));
  }

  @PostMapping("modify")
  public String modify(RedirectAttributes ra, MemberDTO memberDTO, PageRequestDTO pageRequestDTO) {
    Long mid = memberService.modify(memberDTO);

    ra.addFlashAttribute("msg", mid + "번 글이 수정되었습니다."); //일회성
    ra.addAttribute("mid", mid);
    ra.addAttribute("page", pageRequestDTO.getPage());
    ra.addAttribute("type", pageRequestDTO.getType());
    ra.addAttribute("keyword", pageRequestDTO.getKeyword());
    return "redirect:/guestbook/read";
  }

  @PostMapping("remove")
  public String remove(RedirectAttributes ra, PageRequestDTO pageRequestDTO, Long mid) {
    Long tmp = memberService.remove(mid);
    if (tmp != null) {
      if (memberService.getList(pageRequestDTO).getDtoList().size() == 0
      && pageRequestDTO.getPage() != 1) {
        pageRequestDTO.setPage(pageRequestDTO.getPage() - 1);
      }
    }
    ra.addFlashAttribute("msg", tmp != null ? mid + " 번 글이 삭제되었습니다." : "삭제 실패");
    ra.addAttribute("page", pageRequestDTO.getPage());
    ra.addAttribute("type", pageRequestDTO.getType());
    ra.addAttribute("keyword", pageRequestDTO.getKeyword());
    return "redirect:/guestbook/list";
  }
}
// 뷰를 보낸다 (@Controller)
// 데이터만 보낸다 (@RestController)