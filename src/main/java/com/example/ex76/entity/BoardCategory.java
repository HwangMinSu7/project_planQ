package com.example.ex76.entity;

public enum BoardCategory {
  NOTICE("공지"), FREE("자유"), QUEST("퀘스트 인증"), MEETUP("모임");

  private final String label;

  BoardCategory(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }
}
