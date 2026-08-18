package com.example.ex76.dto;

import com.example.ex76.entity.MissionCategory;
import lombok.Data;

@Data
public class MissionSuggestionForm {
  private String title;
  private String description;
  private MissionCategory category = MissionCategory.DAILY;
  private int bountyPoints;
}
