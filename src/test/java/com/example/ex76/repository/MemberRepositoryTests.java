package com.example.ex76.repository;

import com.example.ex76.entity.Member;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;

import java.util.stream.IntStream;

@SpringBootTest
class MemberRepositoryTests {

  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private ReviewRepository reviewRepository;

  @Test
  public void insertMembers() {
    IntStream.rangeClosed(1, 100).forEach(i -> {
      Member member = Member.builder()
          .email("user" + i + "@a.a")
          .pw("1")
          .nickname("reviewer" + i)
          .build();
      memberRepository.save(member);
    });
  }

  @Test
  @Commit
  @Transactional
  public void testDeleteMembers() {
    // memberRepository.deleteById(2l);
    // reviewRepository.deleteByMember(Member.builder().mid(2L).build());

    // 순서 주의
    reviewRepository.deleteByMember(Member.builder().mid(2L).build());
    memberRepository.deleteById(2l);

  }

}