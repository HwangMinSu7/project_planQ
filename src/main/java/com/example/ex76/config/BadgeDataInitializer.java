package com.example.ex76.config;

import com.example.ex76.entity.Badge;
import com.example.ex76.entity.BadgeCode;
import com.example.ex76.repository.BadgeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BadgeDataInitializer implements CommandLineRunner {
  private final BadgeRepository badgeRepository;

  @Override
  public void run(String... args) {
    List<Badge> defaults = List.of(
        badge(BadgeCode.FIRST_QUEST, "첫걸음", "첫 퀘스트를 완료했어요.", "🌱"),
        badge(BadgeCode.STREAK_3, "작심삼일 성공", "3일 연속으로 퀘스트를 완료했어요.", "🔥"),
        badge(BadgeCode.STREAK_7, "일주일의 기적", "7일 연속으로 퀘스트를 완료했어요.", "🏆"),
        badge(BadgeCode.COMMENT_10, "소통왕", "댓글을 10개 작성했어요.", "💬"),
        badge(BadgeCode.FIRST_MEETUP, "모임의 시작", "첫 모임을 개설했어요.", "📅"),
        badge(BadgeCode.JOIN_3, "함께하는 사람", "모임에 3회 참가했어요.", "🤝"),
        badge(BadgeCode.QUEST_10, "계획의 시작", "퀘스트를 10개 완료했어요.", "🧭"),
        badge(BadgeCode.QUEST_30, "플랜 마스터", "퀘스트를 30개 완료했어요.", "👑"),
        badge(BadgeCode.POST_5, "이야기꾼", "게시글을 5개 작성했어요.", "✍️"),
        badge(BadgeCode.LIKE_10, "인기 플래너", "작성한 글에서 좋아요를 10개 받았어요.", "💜"),
        badge(BadgeCode.ALL_CATEGORY, "균형 잡힌 도전자", "모든 퀘스트 카테고리를 완료했어요.", "🌈"),
        badge(BadgeCode.WEEK_CLEAR, "이번 주 올클리어", "이번 주 7일 퀘스트를 모두 완료했어요.", "⭐")
    );
    defaults.stream()
        .filter(badge -> !badgeRepository.existsByCode(badge.getCode()))
        .forEach(badgeRepository::save);
  }

  private Badge badge(BadgeCode code, String name, String description, String icon) {
    return Badge.builder().code(code).name(name).description(description).icon(icon).build();
  }
}
