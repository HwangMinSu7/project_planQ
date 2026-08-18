package com.example.ex76.dto;

import com.example.ex76.entity.BoardCategory;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Data
public class BoardPostForm {
  private BoardCategory category = BoardCategory.FREE;
  private String title;
  private String content;
  private MultipartFile image;
  private boolean removeImage;
  private boolean hasImage;

  @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
  private LocalDateTime meetingAt;

  private String meetingPlace;
  private Integer maxParticipants;
}
