package com.example.ex76.dto;

import java.time.LocalDate;

public record WeeklyQuestDTO(
    LocalDate date,
    String dayLabel,
    boolean completed,
    boolean today
) {}
