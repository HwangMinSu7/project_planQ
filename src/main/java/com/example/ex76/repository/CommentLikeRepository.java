package com.example.ex76.repository;

import com.example.ex76.entity.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
  Optional<CommentLike> findByComment_IdAndMember_Email(Long commentId, String email);

  @Query("select cl.comment.id from CommentLike cl " +
      "where cl.comment.post.id = :postId and cl.member.email = :email")
  List<Long> findLikedCommentIds(@Param("postId") Long postId, @Param("email") String email);
}
