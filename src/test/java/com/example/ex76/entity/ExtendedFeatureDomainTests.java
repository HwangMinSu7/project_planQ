package com.example.ex76.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExtendedFeatureDomainTests {

  @Test
  void likeCounterNeverGoesBelowZero() {
    BoardPost post = BoardPost.builder()
        .category(BoardCategory.FREE).title("test").content("content").build();

    post.addLike();
    post.addLike();
    post.removeLike();
    post.removeLike();
    post.removeLike();

    assertEquals(0, post.getLikeCount());
  }

  @Test
  void missionSuggestionCanBeApprovedOnlyOnce() {
    MissionSuggestion suggestion = MissionSuggestion.builder()
        .title("산책하기")
        .description("10분 산책")
        .category(MissionCategory.HEALTH)
        .build();

    suggestion.approve("admin@onequest.local");

    assertEquals(SuggestionStatus.APPROVED, suggestion.getStatus());
    assertEquals("admin@onequest.local", suggestion.getReviewerEmail());
    assertNotNull(suggestion.getReviewedAt());
    assertThrows(IllegalStateException.class,
        () -> suggestion.reject("admin@onequest.local"));
  }
}
