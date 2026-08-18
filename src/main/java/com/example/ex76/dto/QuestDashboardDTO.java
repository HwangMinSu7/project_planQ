package com.example.ex76.dto;

import com.example.ex76.entity.ClubMember;
import com.example.ex76.entity.DailyQuest;

import java.util.List;

public record QuestDashboardDTO(
    ClubMember member,
    DailyQuest today,
    List<DailyQuest> history,
    List<DailyQuest> communityProofs
) {}
