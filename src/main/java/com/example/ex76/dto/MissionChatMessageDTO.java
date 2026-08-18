package com.example.ex76.dto;

import java.time.LocalDateTime;

public record MissionChatMessageDTO(
    Long id,
    String authorId,
    String authorName,
    String content,
    LocalDateTime sentAt
) {}
