package com.example.ex76.entity;

public enum SuggestionStatus {
  PENDING("검토 중"), APPROVED("승인"), REJECTED("반려");

  private final String label;

  SuggestionStatus(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }
}
