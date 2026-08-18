package com.example.ex76.controller;

import com.example.ex76.dto.BoardPostForm;
import com.example.ex76.dto.LikeResult;
import com.example.ex76.entity.BoardCategory;
import com.example.ex76.service.CommunityService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/community")
@RequiredArgsConstructor
public class CommunityController {
  private final CommunityService communityService;

  @GetMapping
  public String list(@RequestParam(required = false) BoardCategory category,
                     @RequestParam(defaultValue = "") String keyword,
                     @RequestParam(defaultValue = "0") int page,
                     @RequestParam(defaultValue = "list") String view, Model model) {
    String viewMode = "card".equals(view) ? "card" : "list";
    model.addAttribute("posts", communityService.getPosts(category, keyword, page));
    model.addAttribute("selectedCategory", category);
    model.addAttribute("keyword", keyword);
    model.addAttribute("categories", BoardCategory.values());
    model.addAttribute("viewMode", viewMode);
    model.addAttribute("pinnedPosts", communityService.getPinnedPosts());
    return "community/list";
  }

  @GetMapping("/new")
  public String createForm(@RequestParam(required = false) BoardCategory category,
                           Authentication auth, Model model) {
    List<BoardCategory> categories = writableCategories(auth);
    if (!model.containsAttribute("form")) {
      BoardPostForm form = new BoardPostForm();
      if (category != null && categories.contains(category)) form.setCategory(category);
      model.addAttribute("form", form);
    }
    model.addAttribute("categories", categories);
    model.addAttribute("editMode", false);
    return "community/form";
  }

  @PostMapping
  public String create(BoardPostForm form, Authentication auth, RedirectAttributes ra) {
    try {
      Long id = communityService.create(auth.getName(), form);
      ra.addFlashAttribute("msg", "게시글이 등록되었습니다.");
      return "redirect:/community/" + id;
    } catch (RuntimeException e) {
      form.setImage(null);
      ra.addFlashAttribute("error", e.getMessage());
      ra.addFlashAttribute("form", form);
      return "redirect:/community/new";
    }
  }

  @GetMapping("/{postId}")
  public String detail(@PathVariable Long postId, Authentication auth, Model model) {
    model.addAttribute("detail", communityService.getDetail(postId, auth.getName()));
    return "community/detail";
  }

  @GetMapping("/{postId}/image")
  public ResponseEntity<?> image(@PathVariable Long postId) {
    var image = communityService.getPostImage(postId);
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(image.contentType()))
        .body(image.resource());
  }

  @GetMapping("/{postId}/edit")
  public String editForm(@PathVariable Long postId, Authentication auth, Model model) {
    if (!model.containsAttribute("form")) {
      model.addAttribute("form", communityService.getEditForm(postId, auth.getName()));
    }
    model.addAttribute("postId", postId);
    model.addAttribute("categories", writableCategories(auth));
    model.addAttribute("editMode", true);
    return "community/form";
  }

  @PostMapping("/{postId}/edit")
  public String update(@PathVariable Long postId, BoardPostForm form,
                       Authentication auth, RedirectAttributes ra) {
    try {
      communityService.update(postId, auth.getName(), form);
      ra.addFlashAttribute("msg", "게시글을 수정했습니다.");
      return "redirect:/community/" + postId;
    } catch (RuntimeException e) {
      form.setImage(null);
      ra.addFlashAttribute("error", e.getMessage());
      try {
        form.setHasImage(communityService.getEditForm(postId, auth.getName()).isHasImage());
        ra.addFlashAttribute("form", form);
        return "redirect:/community/" + postId + "/edit";
      } catch (RuntimeException denied) {
        return "redirect:/community/" + postId;
      }
    }
  }

  @PostMapping("/{postId}/delete")
  public String delete(@PathVariable Long postId, Authentication auth, RedirectAttributes ra) {
    communityService.delete(postId, auth.getName());
    ra.addFlashAttribute("msg", "게시글을 삭제했습니다.");
    return "redirect:/community";
  }

  @PostMapping("/{postId}/comments")
  public String addComment(@PathVariable Long postId, @RequestParam String content,
                           Authentication auth, RedirectAttributes ra) {
    runAction(() -> communityService.addComment(postId, auth.getName(), content), ra, "댓글을 등록했습니다.");
    return "redirect:/community/" + postId;
  }

  @PostMapping("/{postId}/comments/{commentId}/delete")
  public String deleteComment(@PathVariable Long postId, @PathVariable Long commentId,
                              Authentication auth, RedirectAttributes ra) {
    runAction(() -> communityService.deleteComment(postId, commentId, auth.getName()), ra, "댓글을 삭제했습니다.");
    return "redirect:/community/" + postId;
  }

  @PostMapping("/{postId}/comments/{commentId}/like")
  public String toggleCommentLike(@PathVariable Long postId, @PathVariable Long commentId,
                                  Authentication auth, RedirectAttributes ra) {
    try {
      LikeResult result = communityService.toggleCommentLike(postId, commentId, auth.getName());
      ra.addFlashAttribute("msg", result.liked() ? "댓글을 좋아합니다." : "댓글 좋아요를 취소했습니다.");
    } catch (RuntimeException e) {
      ra.addFlashAttribute("error", e.getMessage());
    }
    return "redirect:/community/" + postId;
  }

  @PostMapping("/{postId}/comments/{commentId}/pin")
  public String toggleCommentPin(@PathVariable Long postId, @PathVariable Long commentId,
                                 Authentication auth, RedirectAttributes ra) {
    runAction(() -> communityService.toggleCommentPin(postId, commentId, auth.getName()),
        ra, "댓글 고정 상태를 변경했습니다.");
    return "redirect:/community/" + postId;
  }

  @PostMapping("/{postId}/like")
  public String toggleLike(@PathVariable Long postId, Authentication auth, RedirectAttributes ra) {
    try {
      LikeResult result = communityService.toggleLike(postId, auth.getName());
      ra.addFlashAttribute("msg", result.liked() ? "게시글을 좋아합니다." : "좋아요를 취소했습니다.");
    } catch (RuntimeException e) {
      ra.addFlashAttribute("error", e.getMessage());
    }
    return "redirect:/community/" + postId;
  }

  @PostMapping("/{postId}/pin")
  public String togglePostPin(@PathVariable Long postId, Authentication auth, RedirectAttributes ra) {
    runAction(() -> communityService.togglePostPin(postId, auth.getName()),
        ra, "게시글 고정 상태를 변경했습니다.");
    return "redirect:/community/" + postId;
  }

  @PostMapping("/pinned/reorder")
  public String reorderPinned(@RequestParam("postIds") List<Long> postIds,
                              Authentication auth, RedirectAttributes ra) {
    runAction(() -> communityService.reorderPinnedPosts(postIds, auth.getName()),
        ra, "고정 게시글 순서를 저장했습니다.");
    return "redirect:/community";
  }

  @PostMapping("/{postId}/join")
  public String join(@PathVariable Long postId, Authentication auth, RedirectAttributes ra) {
    runAction(() -> communityService.join(postId, auth.getName()), ra, "모임에 참가했습니다.");
    return "redirect:/community/" + postId;
  }

  @PostMapping("/{postId}/leave")
  public String leave(@PathVariable Long postId, Authentication auth, RedirectAttributes ra) {
    runAction(() -> communityService.leave(postId, auth.getName()), ra, "모임 참가를 취소했습니다.");
    return "redirect:/community/" + postId;
  }

  private void runAction(Runnable action, RedirectAttributes ra, String successMessage) {
    try {
      action.run();
      ra.addFlashAttribute("msg", successMessage);
    } catch (RuntimeException e) {
      ra.addFlashAttribute("error", e.getMessage());
    }
  }

  private List<BoardCategory> writableCategories(Authentication auth) {
    if (auth.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()))) {
      return Arrays.asList(BoardCategory.values());
    }
    return Arrays.stream(BoardCategory.values())
        .filter(category -> category != BoardCategory.NOTICE)
        .toList();
  }
}
