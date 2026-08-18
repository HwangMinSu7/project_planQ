package com.example.ex76.dto;

import com.example.ex76.entity.*;

import java.util.List;

public record MyPageDTO(
    ClubMember member,
    long completedQuestCount,
    long postCount,
    long commentCount,
    long joinedMeetupCount,
    int levelProgressPercent,
    int pointsToNextLevel,
    List<WeeklyQuestDTO> weeklyQuests,
    long weeklyCompletedCount,
    List<CategoryStatDTO> categoryStats,
    List<DailyQuest> recentQuests,
    List<BoardPost> recentPosts,
    List<MeetupParticipant> upcomingMeetups,
    List<MemberBadge> badges,
    Badge featuredBadge
) {}
