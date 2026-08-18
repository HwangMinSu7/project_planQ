package com.example.ex76.controller;

import com.example.ex76.dto.UploadResultDTO;
import com.example.ex76.service.MovieService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.coobird.thumbnailator.Thumbnailator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@Log4j2
@RequiredArgsConstructor
public class UploadController {
  private final MovieService movieService;

  @Value("${com.example.upload.path}")
  private String uploadPath;

  @PostMapping("/uploadAjax")
  public ResponseEntity<List<UploadResultDTO>> uploadFile(MultipartFile[] uploadFiles) {
    // 전송결과를 리턴해주기 위한 객체
    List<UploadResultDTO> uploadResultDTOList = new ArrayList<>();

    for (MultipartFile uploadFile : uploadFiles) {
      // 확장자나 Content-Type만 보지 않고 실제로 읽을 수 있는 이미지인지 확인한다.
      if (!isImage(uploadFile)) {
        log.warn("This file is not image type.");
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
      }

      // 실제 파일 이름::IE, Edge는 전체 경로가 넘어오기 때문
      String originalName = uploadFile.getOriginalFilename() == null
          ? "image.jpg" : uploadFile.getOriginalFilename(); // 경로포함 파일명
      String fileName = originalName.substring(originalName.lastIndexOf("\\") + 1);
      log.info("fileName: " + fileName); // 실제 파일명만 출력

      // 저장될 경로 생성 :: c:\\upload\\2026\\05\\18
      String folderPath = makeFolder();
      // 유니크한 파일명을 위한 uuid
      String uuid = UUID.randomUUID().toString();

      // 실제 서버 저장될 경로와 파일명 c:\\upload\\2026\\05\\18\\uuid_fileName
      String saveName = uploadPath + File.separator
          + folderPath + File.separator + uuid + "_" + fileName;
      Path savePath = Paths.get(saveName);// saveName을 실제 파일로 생성준비하는 객체

      try {
        uploadFile.transferTo(savePath); // 원본 이미지를 지정 경로에 생성
        String thumbnailSaveName = uploadPath + File.separator
            + folderPath + File.separator + "s_" + uuid + "_" + fileName;
        File thumbnailFile = new File(thumbnailSaveName);// 파일 생성위한 객체 생성
        if (!thumbnailFile.exists()) {
          Thumbnailator.createThumbnail(savePath.toFile(), thumbnailFile, 100, 100);
        }
        uploadResultDTOList.add(new UploadResultDTO(fileName, uuid, folderPath));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return new ResponseEntity<>(uploadResultDTOList, HttpStatus.OK);
  }

  private String makeFolder() {
    String str = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
    // '/'를 운영체제에 맞게 파일 구분자로 변경
    String folderPath = str.replace("/", File.separator);
    File uploadPathFolder = new File(uploadPath, folderPath);
    if (!uploadPathFolder.exists()) uploadPathFolder.mkdirs();

    return folderPath;
  }

  @GetMapping("/display")
  public ResponseEntity<byte[]> getFile(String fileName, String size) {
    try {
      Path file = safePath(fileName);

      if ("1".equals(size)) {
        String name = file.getFileName().toString();
        if (!name.startsWith("s_") || name.length() <= 2) {
          return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        // thumbnail의 파일명 s_를 제거하고 들고 오겠다는 것
        file = file.resolveSibling(name.substring(2)).normalize();
        if (!file.startsWith(uploadRoot())) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
      }

      if (!Files.isRegularFile(file)) return new ResponseEntity<>(HttpStatus.NOT_FOUND);

      HttpHeaders headers = new HttpHeaders(); //브라우저에 전송할때 Header 필요
      String contentType = Files.probeContentType(file);
      headers.add("Content-Type", contentType == null ? "application/octet-stream" : contentType);
      return new ResponseEntity<>(FileCopyUtils.copyToByteArray(file.toFile()), headers, HttpStatus.OK);
    } catch (IllegalArgumentException e) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    } catch (IOException e) {
      log.error("파일을 읽지 못했습니다.", e);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @PostMapping("/removeFile")
  public ResponseEntity<Boolean> removeFile(String fileName, String uuid) {
    log.info(">>>"+fileName);

    try {
      Path file = safePath(fileName);
      Path thumbnail = file.resolveSibling("s_" + file.getFileName()).normalize();
      if (!thumbnail.startsWith(uploadRoot())) return new ResponseEntity<>(false, HttpStatus.BAD_REQUEST);

      if (uuid != null) movieService.removeMovieImagebyUUID(uuid);
      boolean result = Files.deleteIfExists(file);
      if (Files.exists(thumbnail)) result = Files.deleteIfExists(thumbnail) && result;
      return new ResponseEntity<>(result,
          result ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR);
    } catch (IllegalArgumentException e) {
      return new ResponseEntity<>(false, HttpStatus.BAD_REQUEST);
    } catch (IOException e) {
      log.error("파일을 삭제하지 못했습니다.", e);
      return new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  private boolean isImage(MultipartFile file) {
    if (file == null || file.isEmpty() || file.getContentType() == null
        || !file.getContentType().startsWith("image/")) return false;
    try {
      return ImageIO.read(file.getInputStream()) != null;
    } catch (IOException e) {
      return false;
    }
  }

  private Path uploadRoot() {
    return Paths.get(uploadPath).toAbsolutePath().normalize();
  }

  private Path safePath(String fileName) {
    if (fileName == null || fileName.isBlank()) {
      throw new IllegalArgumentException("파일 이름이 없습니다.");
    }
    String decoded = URLDecoder.decode(fileName, StandardCharsets.UTF_8);
    Path target = uploadRoot().resolve(decoded).normalize();
    if (!target.startsWith(uploadRoot())) {
      throw new IllegalArgumentException("잘못된 파일 경로입니다.");
    }
    return target;
  }
}
