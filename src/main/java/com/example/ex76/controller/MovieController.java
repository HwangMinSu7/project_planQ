package com.example.ex76.controller;

import com.example.ex76.dto.MemberDTO;
import com.example.ex76.dto.MovieDTO;
import com.example.ex76.dto.PageRequestDTO;
import com.example.ex76.service.MovieService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/movie")
@Log4j2
@RequiredArgsConstructor
public class MovieController {
  private final MovieService movieService;

  @GetMapping({"", "/", "list"})
  public String list(PageRequestDTO pageRequestDTO, Model model, HttpSession session) {
    MemberDTO memberDTO = (MemberDTO) session.getAttribute("loginMember");
    if (memberDTO == null) {return "redirect:/";}
    model.addAttribute("pageResultDTO", movieService.getList(pageRequestDTO));
    return "/movie/list";
  }

  @GetMapping("register")
  public void register(){} // 단순히 레지스터 페이지로 이동하는것

  @PostMapping("register")
  public String register(MovieDTO movieDTO, RedirectAttributes ra) {
    Long mno = movieService.register(movieDTO);
    ra.addFlashAttribute("msg", mno + "번 Movie가 등록되었습니다.");
    return "redirect:/movie/list";
  }

  @GetMapping({"read", "modify"})
  public void get(Long mno, PageRequestDTO pageRequestDTO, Model model) {
    model.addAttribute("movieDTO", movieService.get(mno));
  }

  @PostMapping("/modify")
  public String modify(MovieDTO movieDTO, RedirectAttributes ra, PageRequestDTO pageRequestDTO) {
    log.info("modify.... movieDTO:" + movieDTO); //movieDTO에는 mno, title, imageDTOList 가 넘어옴
    movieService.modify(movieDTO); // service 이동
    ra.addFlashAttribute("msg", movieDTO.getMno() + " 수정");
    ra.addAttribute("mno", movieDTO.getMno());
    ra.addAttribute("page", pageRequestDTO.getPage());
    ra.addAttribute("type", pageRequestDTO.getType());
    ra.addAttribute("keyword", pageRequestDTO.getKeyword());
    return "redirect:/movie/read";
  }

  @PostMapping("/remove")
  public String remove(Long mno, RedirectAttributes ra, PageRequestDTO pageRequestDTO){
    log.info("remove post... mno: " + mno);
    movieService.removeWithReviewsAndMovieImages(mno);

    if(movieService.getList(pageRequestDTO).getDtoList().size() == 0 && pageRequestDTO.getPage() != 1) {
      pageRequestDTO.setPage(pageRequestDTO.getPage()-1);
    }
    ra.addFlashAttribute("msg", mno + " 삭제");
    ra.addAttribute("page", pageRequestDTO.getPage());
    ra.addAttribute("type", pageRequestDTO.getType());
    ra.addAttribute("keyword", pageRequestDTO.getKeyword());
    return "redirect:/movie/list";
  }
}