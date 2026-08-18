package com.example.ex76.entity;

public enum ReportStatus {
  PENDING("처리 대기"), RESOLVED("처리 완료"), DISMISSED("문제 없음");

  private final String label;
  ReportStatus(String label) { this.label = label; }
  public String getLabel() { return label; }
}
