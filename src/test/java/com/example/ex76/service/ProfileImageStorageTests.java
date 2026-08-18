package com.example.ex76.service;

import com.example.ex76.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ProfileImageStorageTests {

  @TempDir Path tempDir;

  @Test
  void profileImageCanBeSavedLoadedAndDeleted() throws Exception {
    ProfileImageStorage storage = new ProfileImageStorage(tempDir.toString());
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), "png", output);
    MockMultipartFile image = new MockMultipartFile(
        "profileImage", "profile.png", "image/png", output.toByteArray());

    String savedPath = storage.save(image);

    assertTrue(storage.load(savedPath).resource().exists());
    storage.delete(savedPath);
    assertThrows(NotFoundException.class, () -> storage.load(savedPath));
  }
}
