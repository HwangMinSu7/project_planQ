package com.example.ex76.service;

import com.example.ex76.dto.BoardPostForm;
import com.example.ex76.entity.BoardCategory;
import com.example.ex76.entity.BoardPost;
import com.example.ex76.entity.ClubMember;
import com.example.ex76.entity.ClubMemberRole;
import com.example.ex76.exception.NotFoundException;
import com.example.ex76.repository.BoardPostRepository;
import com.example.ex76.repository.ClubMemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CommunityPostImageIntegrationTests {
  @TempDir static Path tempDir;

  @DynamicPropertySource
  static void uploadPath(DynamicPropertyRegistry registry) {
    registry.add("onequest.upload.path", tempDir::toString);
  }

  @Autowired CommunityService communityService;
  @Autowired CommunityImageStorage imageStorage;
  @Autowired ClubMemberRepository memberRepository;
  @Autowired BoardPostRepository postRepository;

  @Test
  void postImageCanBeCreatedReplacedAndRemoved() throws Exception {
    ClubMember member = saveMember();
    BoardPostForm createForm = basicForm();
    createForm.setImage(image("first.png", 0x7357ff));

    Long postId = communityService.create(member.getEmail(), createForm);
    BoardPost created = postRepository.findById(postId).orElseThrow();
    String firstPath = created.getImagePath();

    assertNotNull(firstPath);
    assertTrue(imageStorage.load(firstPath).resource().exists());

    BoardPostForm replaceForm = communityService.getEditForm(postId, member.getEmail());
    replaceForm.setImage(image("second.png", 0xc9ff47));
    communityService.update(postId, member.getEmail(), replaceForm);
    String secondPath = created.getImagePath();

    assertNotEquals(firstPath, secondPath);
    assertThrows(NotFoundException.class, () -> imageStorage.load(firstPath));
    assertTrue(imageStorage.load(secondPath).resource().exists());

    BoardPostForm removeForm = communityService.getEditForm(postId, member.getEmail());
    removeForm.setRemoveImage(true);
    communityService.update(postId, member.getEmail(), removeForm);

    assertNull(created.getImagePath());
    assertThrows(NotFoundException.class, () -> imageStorage.load(secondPath));
  }

  @Test
  void fakeImageFileIsRejected() {
    MockMultipartFile fake = new MockMultipartFile(
        "image", "fake.png", "image/png", "not an image".getBytes());

    assertThrows(IllegalArgumentException.class, () -> imageStorage.save(fake));
  }

  private ClubMember saveMember() {
    ClubMember member = ClubMember.builder()
        .email("community-image-" + UUID.randomUUID() + "@test.local")
        .password("encoded").name("사진 테스트").build();
    member.addMemberRole(ClubMemberRole.USER);
    return memberRepository.save(member);
  }

  private BoardPostForm basicForm() {
    BoardPostForm form = new BoardPostForm();
    form.setCategory(BoardCategory.FREE);
    form.setTitle("사진이 있는 게시글");
    form.setContent("커뮤니티 사진 업로드를 테스트합니다.");
    return form;
  }

  private MockMultipartFile image(String name, int color) throws Exception {
    BufferedImage bufferedImage = new BufferedImage(3, 3, BufferedImage.TYPE_INT_RGB);
    for (int x = 0; x < 3; x++) {
      for (int y = 0; y < 3; y++) bufferedImage.setRGB(x, y, color);
    }
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    ImageIO.write(bufferedImage, "png", output);
    return new MockMultipartFile("image", name, "image/png", output.toByteArray());
  }
}
