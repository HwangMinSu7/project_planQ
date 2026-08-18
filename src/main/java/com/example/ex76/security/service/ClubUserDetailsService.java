package com.example.ex76.security.service;

import com.example.ex76.entity.ClubMember;
import com.example.ex76.repository.ClubMemberRepository;
import com.example.ex76.security.dto.ClubAuthMemberDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
public class ClubUserDetailsService implements UserDetailsService {
  private final ClubMemberRepository clubMemberRepository;

  // 1. 로그인에 사용된 username으로 DB에서 찾는다.
  // 2. 없으면 Exception 발생하고 종료
  // 3. 있으면 인스턴스 생성
  // 4. 저장하기 위한 세션 dto에 담고
  // 5. 리턴 dto
  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    String normalizedUsername = username == null ? "" : username.trim().toLowerCase();
    Optional<ClubMember> result = clubMemberRepository.findByEmail(normalizedUsername, false);
    if(!result.isPresent()) throw new UsernameNotFoundException("Check Email or Social");
    ClubMember clubMember = result.get(); // 3
    ClubAuthMemberDTO dto = new ClubAuthMemberDTO(
        clubMember.getEmail(), clubMember.getPassword(), clubMember.isFromSocial(),
        clubMember.getRoleSet().stream().map(
            role -> new SimpleGrantedAuthority("ROLE_" + role.name())
        ).collect(Collectors.toSet()),
        clubMember.getEmail(), clubMember.getName()
    );
    dto.setName(clubMember.getName());
    dto.setFromSocial(clubMember.isFromSocial());

    return dto;
  }
}
