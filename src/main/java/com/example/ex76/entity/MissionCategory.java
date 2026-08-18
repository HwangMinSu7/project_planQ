package com.example.ex76.entity;

public enum MissionCategory {
  DAILY("일상"), HEALTH("건강"), CREATIVE("창작"), SOCIAL("소통"), REFRESH("리프레시");

  private final String label;

  MissionCategory(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }
}
