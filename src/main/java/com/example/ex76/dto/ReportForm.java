package com.example.ex76.dto;

import com.example.ex76.entity.ReportKind;
import com.example.ex76.entity.ReportTargetType;
import lombok.Data;

@Data
public class ReportForm {
  private ReportKind kind = ReportKind.REPORT;
  private ReportTargetType targetType = ReportTargetType.GENERAL;
  private Long targetId;
  private String reason;
}
