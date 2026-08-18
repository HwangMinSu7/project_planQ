package com.example.ex76.security.service;

import com.example.ex76.entity.ClubMember;
import com.example.ex76.entity.ClubMemberRole;
import com.example.ex76.repository.ClubMemberRepository;
import com.example.ex76.security.dto.ClubAuthMemberDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
public class ClubOAuthUserDetailsService extends DefaultOAuth2UserService {
  private final ClubMemberRepository clubMemberRepository;
  private final PasswordEncoder passwordEncoder;

  enum SocialType {KAKAO, NAVER, GOOGLE}

  private SocialType getSocialType(String registrationId) {
    return switch (registrationId) {
      case "NAVER" -> SocialType.NAVER;
      case "KAKAO" -> SocialType.KAKAO;
      default -> SocialType.GOOGLE;
    };
  }

  // 1. Social(구글, 네이버, 카카오, ...)에 로그인정보를 보내고 로그인 결과를 받는다.
  // 2. 결과에서  username과 Social 업체에서 제공하는 관련 정보를 추출
  // 3. username의 email을 가지고 DB에 있는지 확인
  // 4. 있으면 세션을 남기고, 없으면 DB에 저장후  세션을 남김.
  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    // Social 업체 출력
    String registrationId = userRequest.getClientRegistration().getRegistrationId();
    SocialType socialType = getSocialType(registrationId.trim().toString());
    String userNameAttributeName = userRequest.getClientRegistration()
        .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();
    log.info("userNameAttributeName >> " + userNameAttributeName);

    // Social로 부터 넘어온 정보를 출력하는 부분
    OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
    //delegate.loadUser()는 userRequest(소셜에서 온 유저정보)를 세션 객체(OAuth2User)로 변환
    OAuth2User oAuth2User = delegate.loadUser(userRequest);
    oAuth2User.getAttributes().forEach((k, v) -> log.info(">>> " + k + ": " + v));

    String email = null;
    ClubMember  clubMember = null;
    if(socialType.name().equals("Google")) email = oAuth2User.getAttribute("email");
    else email = oAuth2User.getAttribute("email");

    Optional<ClubMember> result = clubMemberRepository.findByEmail(email, true);
    if(result.isPresent()) { // 4. 이미 등록 되어있을 때
      clubMember = result.get();
    } else { // 등록 되지 않을 경우 DB로 저장해야 됨
      clubMember = ClubMember.builder()
          .email(email)
          .name(oAuth2User.getAttribute("name"))
          .password(passwordEncoder.encode("1"))
          .fromSocial(true)
          .build();
      clubMember.addMemberRole(ClubMemberRole.USER);
      clubMemberRepository.save(clubMember); // DB저장
    }
    System.out.println("clubMember: " + clubMember);
    // 세션 저장
    ClubAuthMemberDTO dto = new ClubAuthMemberDTO(
        clubMember.getEmail(), clubMember.getPassword(), true,
        clubMember.getRoleSet().stream().map(
            role -> new SimpleGrantedAuthority("ROLE_" + role.name())
        ).collect(Collectors.toSet()),
        oAuth2User.getAttributes(),
        clubMember.getEmail(), clubMember.getName()
    );
    /*dto.setName(clubMember.getName());
    dto.setFromSocial(clubMember.isFromSocial());*/

    return dto;
  }
}
