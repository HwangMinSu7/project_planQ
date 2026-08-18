package com.example.ex76.config;

import com.example.ex76.entity.ClubMember;
import com.example.ex76.entity.ClubMemberRole;
import com.example.ex76.repository.ClubMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class AdminAccountInitializer implements CommandLineRunner {
  private final ClubMemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  @Transactional
  public void run(String... args) {
    ClubMember admin = memberRepository.findById("admin").orElseGet(() ->
        ClubMember.builder()
            .email("admin")
            .password(passwordEncoder.encode("1234"))
            .name("운영자")
            .fromSocial(false)
            .build());
    admin.changePassword(passwordEncoder.encode("1234"));
    admin.addMemberRole(ClubMemberRole.USER);
    admin.addMemberRole(ClubMemberRole.ADMIN);
    memberRepository.save(admin);
  }
}
