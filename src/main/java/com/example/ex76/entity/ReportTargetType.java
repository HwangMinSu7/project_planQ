package com.example.ex76.entity;

public enum ReportTargetType {
  POST("게시글"), COMMENT("댓글"), SUGGESTION("미션 제안"), GENERAL("일반 문의");

  private final String label;
  ReportTargetType(String label) { this.label = label; }
  public String getLabel() { return label; }
}
