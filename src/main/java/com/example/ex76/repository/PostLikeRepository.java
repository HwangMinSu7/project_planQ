package com.example.ex76.repository;

import com.example.ex76.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
  boolean existsByPost_IdAndMember_Email(Long postId, String email);
  Optional<PostLike> findByPost_IdAndMember_Email(Long postId, String email);
}
