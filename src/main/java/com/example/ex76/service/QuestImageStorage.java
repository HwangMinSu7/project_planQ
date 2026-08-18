package com.example.ex76.service;

import com.example.ex76.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class QuestImageStorage {
  private final Path root;

  public QuestImageStorage(@Value("${onequest.upload.path:${com.example.upload.path}}") String uploadPath) {
    this.root = Path.of(uploadPath).toAbsolutePath().normalize();
  }

  public String save(MultipartFile file) {
    if (file == null || file.isEmpty()) return null;
    if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
      throw new IllegalArgumentException("인증 파일은 이미지만 업로드할 수 있습니다.");
    }
    try {
      if (ImageIO.read(file.getInputStream()) == null) {
        throw new IllegalArgumentException("읽을 수 있는 이미지 파일만 업로드해 주세요.");
      }
    } catch (IOException e) {
      throw new IllegalArgumentException("이미지 파일을 확인하지 못했습니다.", e);
    }

    String original = file.getOriginalFilename() == null ? "proof.jpg" : file.getOriginalFilename();
    String safeName = Path.of(original).getFileName().toString().replaceAll("[^a-zA-Z0-9._가-힣-]", "_");
    LocalDate today = LocalDate.now();
    Path relative = Path.of("quest", String.valueOf(today.getYear()),
        String.format("%02d", today.getMonthValue()), String.format("%02d", today.getDayOfMonth()),
        UUID.randomUUID() + "_" + safeName);
    Path target = root.resolve(relative).normalize();
    if (!target.startsWith(root)) throw new IllegalArgumentException("잘못된 파일 경로입니다.");

    try {
      Files.createDirectories(target.getParent());
      file.transferTo(target);
      return relative.toString().replace('\\', '/');
    } catch (IOException e) {
      throw new IllegalStateException("이미지를 저장하지 못했습니다.", e);
    }
  }

  public StoredImage load(String relativePath) {
    if (relativePath == null || relativePath.isBlank()) {
      throw new NotFoundException("인증 이미지가 없습니다.");
    }
    Path target = root.resolve(relativePath).normalize();
    if (!target.startsWith(root) || !Files.exists(target)) {
      throw new NotFoundException("인증 이미지를 찾을 수 없습니다.");
    }
    try {
      Resource resource = new UrlResource(target.toUri());
      String contentType = Files.probeContentType(target);
      return new StoredImage(resource, contentType == null ? "application/octet-stream" : contentType);
    } catch (MalformedURLException e) {
      throw new IllegalStateException("이미지를 읽지 못했습니다.", e);
    } catch (IOException e) {
      throw new IllegalStateException("이미지 형식을 확인하지 못했습니다.", e);
    }
  }

  public record StoredImage(Resource resource, String contentType) {}
}
