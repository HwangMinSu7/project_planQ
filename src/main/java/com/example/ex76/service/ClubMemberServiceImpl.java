package com.example.ex76.service;

import com.example.ex76.dto.ClubMemberDTO;
import com.example.ex76.entity.ClubMember;
import com.example.ex76.entity.ClubMemberRole;
import com.example.ex76.repository.ClubMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ClubMemberServiceImpl implements ClubMemberService {
  private final ClubMemberRepository clubMemberRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  public String register(ClubMemberDTO dto) {
    if (dto.getUsername() == null || dto.getUsername().isBlank()) {
      throw new IllegalArgumentException("아이디를 입력해 주세요.");
    }
    String username = dto.getUsername().trim().toLowerCase();
    String usernamePattern = "(?=.{4,100}$)(?=.*[a-z])(?=.*[0-9])"
        + "(?:[a-z0-9]+|[a-z0-9]+(?:\\.[a-z0-9]+)*@[a-z0-9]+(?:\\.[a-z0-9]+)+)";
    if (!username.matches(usernamePattern)) {
      throw new IllegalArgumentException(
          "아이디는 영문자와 숫자를 모두 포함하거나, 영문자와 숫자가 포함된 이메일 형식으로 입력해 주세요.");
    }
    if (dto.getPassword() == null || dto.getPassword().length() < 4) {
      throw new IllegalArgumentException("비밀번호는 4자 이상이어야 합니다.");
    }
    if (dto.getPassword().length() > 50) {
      throw new IllegalArgumentException("비밀번호는 50자 이하로 입력해 주세요.");
    }
    if (!dto.getPassword().equals(dto.getPasswordConfirm())) {
      throw new IllegalArgumentException("비밀번호 확인이 일치하지 않습니다.");
    }
    if (dto.getName() != null && dto.getName().trim().length() > 30) {
      throw new IllegalArgumentException("닉네임은 30자 이하로 입력해 주세요.");
    }

    if (clubMemberRepository.existsById(username)) {
      throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
    }

    ClubMember member = ClubMember.builder()
        .email(username)
        .password(passwordEncoder.encode(dto.getPassword()))
        .name(dto.getName() == null || dto.getName().isBlank() ? "퀘스터" : dto.getName().trim())
        .fromSocial(false)
        .build();
    member.addMemberRole(ClubMemberRole.USER);
    return clubMemberRepository.save(member).getEmail();
  }

  @Override
  @Transactional(readOnly = true)
  public boolean exists(String email) {
    return email != null && clubMemberRepository.existsById(email.trim().toLowerCase());
  }

  @Override
  @Transactional(readOnly = true)
  public ClubMemberDTO getProfile(String loginEmail) {
    ClubMember member = clubMemberRepository.findById(loginEmail)
        .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
    return ClubMemberDTO.builder()
        .email(member.getEmail())
        .name(member.getName())
        .fromSocial(member.isFromSocial())
        .build();
  }

  @Override
  public String modify(String loginEmail, ClubMemberDTO dto) {
    // 화면에서 넘어온 회원 식별값을 믿지 않고 실제 로그인한 회원을 수정한다.
    ClubMember member = clubMemberRepository.findById(loginEmail)
        .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
    boolean wantsPasswordChange = (dto.getCurrentPassword() != null && !dto.getCurrentPassword().isBlank())
        || (dto.getNewPassword() != null && !dto.getNewPassword().isBlank())
        || (dto.getNewPasswordConfirm() != null && !dto.getNewPasswordConfirm().isBlank());
    if (wantsPasswordChange) {
      if (dto.getCurrentPassword() == null
          || !passwordEncoder.matches(dto.getCurrentPassword(), member.getPassword())) {
        throw new IllegalArgumentException("현재 비밀번호가 올바르지 않습니다.");
      }
      if (dto.getNewPassword() == null
          || dto.getNewPassword().length() < 4 || dto.getNewPassword().length() > 50) {
        throw new IllegalArgumentException("비밀번호는 4자 이상 50자 이하로 입력해 주세요.");
      }
      if (!dto.getNewPassword().equals(dto.getNewPasswordConfirm())) {
        throw new IllegalArgumentException("새 비밀번호 확인이 일치하지 않습니다.");
      }
      member.changePassword(passwordEncoder.encode(dto.getNewPassword()));
    }
    if (dto.getName() != null && !dto.getName().isBlank()) {
      if (dto.getName().trim().length() > 30) {
        throw new IllegalArgumentException("닉네임은 30자 이하로 입력해 주세요.");
      }
      member.changeName(dto.getName().trim());
    }
    return member.getEmail();
  }

  @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
  public void userAccess() {}

  @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
  public void managerAccess() {}

  @PreAuthorize("hasRole('ADMIN')")
  public void adminAccess() {}

  @PreAuthorize("#username == authentication.name")
  public void selfAccess(String username) {}
}
