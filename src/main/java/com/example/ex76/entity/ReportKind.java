package com.example.ex76.entity;

public enum ReportKind {
  REPORT("신고"), INQUIRY("문의");

  private final String label;
  ReportKind(String label) { this.label = label; }
  public String getLabel() { return label; }
}
