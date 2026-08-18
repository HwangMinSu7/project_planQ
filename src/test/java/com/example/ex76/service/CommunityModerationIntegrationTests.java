package com.example.ex76.service;

import com.example.ex76.dto.BoardDetailDTO;
import com.example.ex76.dto.BoardPostForm;
import com.example.ex76.dto.LikeResult;
import com.example.ex76.entity.*;
import com.example.ex76.repository.BoardCommentRepository;
import com.example.ex76.repository.BoardPostRepository;
import com.example.ex76.repository.ClubMemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CommunityModerationIntegrationTests {
  @Autowired CommunityService communityService;
  @Autowired ClubMemberRepository memberRepository;
  @Autowired BoardPostRepository postRepository;
  @Autowired BoardCommentRepository commentRepository;
  @Autowired PasswordEncoder passwordEncoder;

  @Test
  void adminAccountUsesRequestedLoginAndRole() {
    ClubMember admin = memberRepository.findById("admin").orElseThrow();

    assertEquals("운영자", admin.getName());
    assertTrue(passwordEncoder.matches("1234", admin.getPassword()));
    assertTrue(admin.getRoleSet().contains(ClubMemberRole.ADMIN));
  }

  @Test
  void noticeAndPinnedPostAreOrderedAndProtectedByAdminRole() {
    ClubMember admin = memberRepository.findById("admin").orElseThrow();
    ClubMember user = saveMember("일반 회원", false);
    String token = "moderation-" + UUID.randomUUID();

    BoardPostForm forbiddenNotice = form(BoardCategory.NOTICE, token + " forbidden");
    assertThrows(IllegalStateException.class,
        () -> communityService.create(user.getEmail(), forbiddenNotice));

    Long normalId = communityService.create(user.getEmail(), form(BoardCategory.FREE, token + " normal"));
    Long pinnedId = communityService.create(user.getEmail(), form(BoardCategory.QUEST, token + " pinned"));
    Long secondPinnedId = communityService.create(user.getEmail(), form(BoardCategory.FREE, token + " pinned two"));
    Long noticeId = communityService.create(admin.getEmail(), form(BoardCategory.NOTICE, token + " notice"));

    assertThrows(IllegalStateException.class,
        () -> communityService.togglePostPin(pinnedId, user.getEmail()));
    communityService.togglePostPin(pinnedId, admin.getEmail());
    communityService.togglePostPin(secondPinnedId, admin.getEmail());

    List<BoardPost> ordered = communityService.getPosts(null, token, 0).getContent();
    assertEquals(List.of(noticeId, pinnedId, secondPinnedId, normalId),
        ordered.stream().map(BoardPost::getId).toList());
    List<Long> reversedPinOrder = communityService.getPinnedPosts().stream()
        .map(BoardPost::getId).collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));
    java.util.Collections.reverse(reversedPinOrder);
    communityService.reorderPinnedPosts(reversedPinOrder, admin.getEmail());
    for (int i = 0; i < reversedPinOrder.size(); i++) {
      assertEquals(i + 1, postRepository.findById(reversedPinOrder.get(i)).orElseThrow().getPinOrder());
    }
    assertTrue(postRepository.findById(noticeId).orElseThrow().isPinned());
    assertThrows(IllegalStateException.class,
        () -> communityService.getEditForm(normalId, admin.getEmail()));
    assertThrows(IllegalStateException.class,
        () -> communityService.update(normalId, admin.getEmail(), form(BoardCategory.FREE, "운영자 수정 시도")));
    communityService.delete(normalId, admin.getEmail());
    assertFalse(postRepository.existsById(normalId));
  }

  @Test
  void postOwnerCanPinOneCommentAndMembersCanToggleCommentLike() {
    ClubMember owner = saveMember("글 작성자", false);
    ClubMember firstWriter = saveMember("첫 댓글", false);
    ClubMember secondWriter = saveMember("둘째 댓글", false);
    ClubMember reader = saveMember("좋아요 회원", false);
    Long postId = communityService.create(owner.getEmail(), form(BoardCategory.FREE, "댓글 기능"));
    communityService.addComment(postId, firstWriter.getEmail(), "첫 번째 댓글");
    communityService.addComment(postId, secondWriter.getEmail(), "두 번째 댓글");
    List<BoardComment> comments = commentRepository.findByPost_IdOrderByPinnedDescIdAsc(postId);
    Long firstId = comments.get(0).getId();
    Long secondId = comments.get(1).getId();

    assertThrows(IllegalStateException.class,
        () -> communityService.toggleCommentPin(postId, firstId, reader.getEmail()));
    communityService.toggleCommentPin(postId, firstId, owner.getEmail());
    communityService.toggleCommentPin(postId, secondId, owner.getEmail());

    assertFalse(commentRepository.findById(firstId).orElseThrow().isPinned());
    assertTrue(commentRepository.findById(secondId).orElseThrow().isPinned());
    assertEquals(secondId, communityService.getDetail(postId, reader.getEmail()).comments().get(0).getId());

    communityService.toggleCommentPin(postId, firstId, "admin");
    assertTrue(commentRepository.findById(firstId).orElseThrow().isPinned());
    assertFalse(commentRepository.findById(secondId).orElseThrow().isPinned());

    LikeResult liked = communityService.toggleCommentLike(postId, secondId, reader.getEmail());
    BoardDetailDTO detail = communityService.getDetail(postId, reader.getEmail());
    assertTrue(liked.liked());
    assertEquals(1, liked.likeCount());
    assertTrue(detail.likedCommentIds().contains(secondId));

    LikeResult removed = communityService.toggleCommentLike(postId, secondId, reader.getEmail());
    assertFalse(removed.liked());
    assertEquals(0, removed.likeCount());

    communityService.deleteComment(postId, secondId, "admin");
    assertFalse(commentRepository.existsById(secondId));
  }

  private ClubMember saveMember(String name, boolean admin) {
    ClubMember member = ClubMember.builder()
        .email("moderation-" + UUID.randomUUID() + "@test.local")
        .password("encoded").name(name).build();
    member.addMemberRole(ClubMemberRole.USER);
    if (admin) member.addMemberRole(ClubMemberRole.ADMIN);
    return memberRepository.save(member);
  }

  private BoardPostForm form(BoardCategory category, String title) {
    BoardPostForm form = new BoardPostForm();
    form.setCategory(category);
    form.setTitle(title);
    form.setContent(title + " 내용");
    return form;
  }
}
