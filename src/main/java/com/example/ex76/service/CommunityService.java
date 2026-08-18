package com.example.ex76.service;

import com.example.ex76.dto.BoardDetailDTO;
import com.example.ex76.dto.BoardPostForm;
import com.example.ex76.dto.LikeResult;
import com.example.ex76.entity.*;
import com.example.ex76.exception.NotFoundException;
import com.example.ex76.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CommunityService {
  private final BoardPostRepository postRepository;
  private final BoardCommentRepository commentRepository;
  private final MeetupParticipantRepository participantRepository;
  private final PostLikeRepository likeRepository;
  private final CommentLikeRepository commentLikeRepository;
  private final ClubMemberRepository memberRepository;
  private final BadgeService badgeService;
  private final CommunityImageStorage imageStorage;

  @Transactional(readOnly = true)
  public Page<BoardPost> getPosts(BoardCategory category, String keyword, int page) {
    PageRequest pageable = PageRequest.of(Math.max(page, 0), 10);
    return postRepository.search(category, keyword == null ? "" : keyword.trim(), pageable);
  }

  @Transactional(readOnly = true)
  public List<BoardPost> getPinnedPosts() {
    return postRepository.findByPinnedTrueOrderByPinOrderAscIdDesc();
  }

  public Long create(String email, BoardPostForm form) {
    ClubMember author = getMember(email);
    validate(form, author);
    String imagePath = imageStorage.save(form.getImage());
    BoardPost post = BoardPost.builder()
        .author(author)
        .category(form.getCategory())
        .title(form.getTitle().trim())
        .content(form.getContent().trim())
        .imagePath(imagePath)
        .meetingAt(form.getCategory() == BoardCategory.MEETUP ? form.getMeetingAt() : null)
        .meetingPlace(form.getCategory() == BoardCategory.MEETUP ? form.getMeetingPlace().trim() : null)
        .maxParticipants(form.getCategory() == BoardCategory.MEETUP ? form.getMaxParticipants() : null)
        .pinned(form.getCategory() == BoardCategory.NOTICE)
        .pinOrder(form.getCategory() == BoardCategory.NOTICE ? nextPinOrder() : 0)
        .build();
    Long id = postRepository.save(post).getId();
    badgeService.evaluate(email);
    return id;
  }

  @Transactional(readOnly = true)
  public BoardDetailDTO getDetail(Long postId, String email) {
    BoardPost post = getPost(postId);
    ClubMember viewer = getMember(email);
    boolean admin = isAdmin(viewer);
    return new BoardDetailDTO(
        post,
        commentRepository.findByPost_IdOrderByPinnedDescIdAsc(postId),
        post.getCategory() == BoardCategory.MEETUP
            ? participantRepository.findByPost_IdOrderByJoinedAtAsc(postId) : List.of(),
        participantRepository.existsByPost_IdAndMember_Email(postId, email),
        post.getAuthor().getEmail().equals(email),
        likeRepository.existsByPost_IdAndMember_Email(postId, email),
        admin,
        new HashSet<>(commentLikeRepository.findLikedCommentIds(postId, email)));
  }

  @Transactional(readOnly = true)
  public BoardPostForm getEditForm(Long postId, String email) {
    BoardPost post = getOwnedPost(postId, email);
    BoardPostForm form = new BoardPostForm();
    form.setCategory(post.getCategory());
    form.setTitle(post.getTitle());
    form.setContent(post.getContent());
    form.setHasImage(post.getImagePath() != null);
    form.setMeetingAt(post.getMeetingAt());
    form.setMeetingPlace(post.getMeetingPlace());
    form.setMaxParticipants(post.getMaxParticipants());
    return form;
  }

  public void update(Long postId, String email, BoardPostForm form) {
    ClubMember actor = getMember(email);
    validate(form, actor);
    BoardPost post = getOwnedPost(postId, actor);
    if (post.getParticipantCount() > 0 && form.getCategory() != BoardCategory.MEETUP) {
      throw new IllegalArgumentException("참가자가 있는 모임 글은 일반 글로 변경할 수 없습니다.");
    }
    if (form.getCategory() == BoardCategory.MEETUP
        && form.getMaxParticipants() < post.getParticipantCount()) {
      throw new IllegalArgumentException("정원은 현재 참가 인원보다 작게 줄일 수 없습니다.");
    }
    post.update(form.getCategory(), form.getTitle().trim(), form.getContent().trim(),
        form.getMeetingAt(), form.getMeetingPlace(), form.getMaxParticipants());
    if (form.getCategory() == BoardCategory.NOTICE && !post.isPinned()) post.pin(nextPinOrder());
    if (form.getImage() != null && !form.getImage().isEmpty()) {
      String oldPath = post.getImagePath();
      post.changeImage(imageStorage.save(form.getImage()));
      imageStorage.delete(oldPath);
    } else if (form.isRemoveImage()) {
      String oldPath = post.getImagePath();
      post.changeImage(null);
      imageStorage.delete(oldPath);
    }
  }

  public void delete(Long postId, String email) {
    BoardPost post = getManageablePost(postId, email);
    postRepository.delete(post);
    imageStorage.delete(post.getImagePath());
  }

  @Transactional(readOnly = true)
  public CommunityImageStorage.StoredImage getPostImage(Long postId) {
    return imageStorage.load(getPost(postId).getImagePath());
  }

  public void addComment(Long postId, String email, String content) {
    if (content == null || content.isBlank()) throw new IllegalArgumentException("댓글을 입력해 주세요.");
    if (content.trim().length() > 600) throw new IllegalArgumentException("댓글은 600자 이하로 입력해 주세요.");
    BoardPost post = getPost(postId);
    commentRepository.save(BoardComment.builder()
        .post(post).author(getMember(email)).content(content.trim()).build());
    post.addComment();
    badgeService.evaluate(email);
  }

  public void deleteComment(Long postId, Long commentId, String email) {
    BoardComment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
    ClubMember actor = getMember(email);
    if (!comment.getPost().getId().equals(postId)
        || (!comment.getAuthor().getEmail().equals(email) && !isAdmin(actor))) {
      throw new IllegalStateException("본인의 댓글만 삭제할 수 있습니다.");
    }
    comment.getPost().removeComment();
    commentRepository.delete(comment);
  }

  public LikeResult toggleLike(Long postId, String email) {
    BoardPost post = getPost(postId);
    PostLike existing = likeRepository.findByPost_IdAndMember_Email(postId, email).orElse(null);
    if (existing != null) {
      likeRepository.delete(existing);
      post.removeLike();
      return new LikeResult(false, post.getLikeCount());
    }

    likeRepository.save(PostLike.builder()
        .post(post).member(getMember(email)).likedAt(LocalDateTime.now()).build());
    post.addLike();
    badgeService.evaluate(post.getAuthor().getEmail());
    return new LikeResult(true, post.getLikeCount());
  }

  public LikeResult toggleCommentLike(Long postId, Long commentId, String email) {
    BoardComment comment = getComment(postId, commentId);
    CommentLike existing = commentLikeRepository
        .findByComment_IdAndMember_Email(commentId, email).orElse(null);
    if (existing != null) {
      commentLikeRepository.delete(existing);
      comment.removeLike();
      return new LikeResult(false, comment.getLikeCount());
    }

    commentLikeRepository.save(CommentLike.builder()
        .comment(comment).member(getMember(email)).likedAt(LocalDateTime.now()).build());
    comment.addLike();
    return new LikeResult(true, comment.getLikeCount());
  }

  public void toggleCommentPin(Long postId, Long commentId, String email) {
    BoardComment comment = getComment(postId, commentId);
    ClubMember actor = getMember(email);
    if (!comment.getPost().getAuthor().getEmail().equals(email) && !isAdmin(actor)) {
      throw new IllegalStateException("게시글 작성자와 운영자만 댓글을 고정할 수 있습니다.");
    }
    if (comment.isPinned()) {
      comment.unpin();
      return;
    }
    commentRepository.findByPost_IdAndPinnedTrue(postId).forEach(BoardComment::unpin);
    comment.pin();
  }

  public void togglePostPin(Long postId, String email) {
    ClubMember actor = getMember(email);
    if (!isAdmin(actor)) throw new IllegalStateException("운영자만 게시글을 고정할 수 있습니다.");
    BoardPost post = getPost(postId);
    if (post.getCategory() == BoardCategory.NOTICE) {
      throw new IllegalStateException("공지사항은 항상 상단에 고정됩니다.");
    }
    if (post.isPinned()) post.unpin();
    else post.pin(nextPinOrder());
  }

  public void reorderPinnedPosts(List<Long> postIds, String email) {
    ClubMember actor = getMember(email);
    if (!isAdmin(actor)) throw new IllegalStateException("운영자만 고정글 순서를 변경할 수 있습니다.");
    List<BoardPost> pinnedPosts = postRepository.findByPinnedTrueOrderByPinOrderAscIdDesc();
    if (postIds == null || postIds.size() != pinnedPosts.size()
        || !new HashSet<>(postIds).equals(pinnedPosts.stream().map(BoardPost::getId).collect(java.util.stream.Collectors.toSet()))) {
      throw new IllegalArgumentException("고정 게시글 목록이 올바르지 않습니다.");
    }
    for (int index = 0; index < postIds.size(); index++) {
      Long postId = postIds.get(index);
      BoardPost post = pinnedPosts.stream()
          .filter(item -> item.getId().equals(postId)).findFirst().orElseThrow();
      post.changePinOrder(index + 1);
    }
  }

  public void join(Long postId, String email) {
    BoardPost post = getPost(postId);
    if (post.getCategory() != BoardCategory.MEETUP) throw new IllegalStateException("모임 글이 아닙니다.");
    if (post.getMeetingAt().isBefore(LocalDateTime.now())) throw new IllegalStateException("이미 종료된 모임입니다.");
    if (participantRepository.existsByPost_IdAndMember_Email(postId, email)) {
      throw new IllegalStateException("이미 참가한 모임입니다.");
    }
    if (post.getParticipantCount() >= post.getMaxParticipants()) {
      throw new IllegalStateException("모임 정원이 가득 찼습니다.");
    }
    participantRepository.save(MeetupParticipant.builder()
        .post(post).member(getMember(email)).joinedAt(LocalDateTime.now()).build());
    post.addParticipant();
    badgeService.evaluate(email);
  }

  public void leave(Long postId, String email) {
    MeetupParticipant participant = participantRepository.findByPost_IdAndMember_Email(postId, email)
        .orElseThrow(() -> new IllegalStateException("참가 중인 모임이 아닙니다."));
    participant.getPost().removeParticipant();
    participantRepository.delete(participant);
  }

  private void validate(BoardPostForm form, ClubMember actor) {
    if (form.getCategory() == null) throw new IllegalArgumentException("게시글 종류를 선택해 주세요.");
    if (form.getCategory() == BoardCategory.NOTICE && !isAdmin(actor)) {
      throw new IllegalStateException("공지사항은 운영자만 작성할 수 있습니다.");
    }
    if (form.getTitle() == null || form.getTitle().isBlank()) throw new IllegalArgumentException("제목을 입력해 주세요.");
    if (form.getContent() == null || form.getContent().isBlank()) throw new IllegalArgumentException("내용을 입력해 주세요.");
    if (form.getTitle().trim().length() > 160) throw new IllegalArgumentException("제목은 160자 이하로 입력해 주세요.");
    if (form.getContent().trim().length() > 5000) throw new IllegalArgumentException("내용은 5000자 이하로 입력해 주세요.");
    if (form.getCategory() == BoardCategory.MEETUP) {
      if (form.getMeetingAt() == null || form.getMeetingAt().isBefore(LocalDateTime.now())) {
        throw new IllegalArgumentException("모임 시간을 현재 이후로 설정해 주세요.");
      }
      if (form.getMeetingPlace() == null || form.getMeetingPlace().isBlank()) {
        throw new IllegalArgumentException("모임 장소를 입력해 주세요.");
      }
      if (form.getMeetingPlace().trim().length() > 150) {
        throw new IllegalArgumentException("모임 장소는 150자 이하로 입력해 주세요.");
      }
      if (form.getMaxParticipants() == null || form.getMaxParticipants() < 2) {
        throw new IllegalArgumentException("모임 정원은 2명 이상이어야 합니다.");
      }
      if (form.getMaxParticipants() > 100) {
        throw new IllegalArgumentException("모임 정원은 100명 이하로 입력해 주세요.");
      }
    }
  }

  private BoardPost getPost(Long postId) {
    return postRepository.findById(postId)
        .orElseThrow(() -> new NotFoundException("게시글을 찾을 수 없습니다."));
  }

  private BoardComment getComment(Long postId, Long commentId) {
    BoardComment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new NotFoundException("댓글을 찾을 수 없습니다."));
    if (!comment.getPost().getId().equals(postId)) {
      throw new IllegalArgumentException("게시글과 댓글 정보가 일치하지 않습니다.");
    }
    return comment;
  }

  private BoardPost getManageablePost(Long postId, String email) {
    return getManageablePost(postId, getMember(email));
  }

  private BoardPost getOwnedPost(Long postId, String email) {
    return getOwnedPost(postId, getMember(email));
  }

  private BoardPost getOwnedPost(Long postId, ClubMember actor) {
    BoardPost post = getPost(postId);
    if (!post.getAuthor().getEmail().equals(actor.getEmail())) {
      throw new IllegalStateException("게시글은 작성자만 수정할 수 있습니다.");
    }
    return post;
  }

  private int nextPinOrder() {
    return postRepository.findMaxPinOrder() + 1;
  }

  private BoardPost getManageablePost(Long postId, ClubMember actor) {
    BoardPost post = getPost(postId);
    if (!post.getAuthor().getEmail().equals(actor.getEmail()) && !isAdmin(actor)) {
      throw new IllegalStateException("본인의 게시글만 수정하거나 삭제할 수 있습니다.");
    }
    return post;
  }

  private boolean isAdmin(ClubMember member) {
    return member.getRoleSet().contains(ClubMemberRole.ADMIN);
  }

  private ClubMember getMember(String email) {
    return memberRepository.findById(email)
        .orElseThrow(() -> new IllegalArgumentException("로그인 회원을 찾을 수 없습니다."));
  }
}
