package com.example.ex76.service;

import com.example.ex76.dto.MemberDTO;
import com.example.ex76.dto.PageRequestDTO;
import com.example.ex76.dto.PageResultDTO;
import com.example.ex76.entity.Member;

public interface MemberService {
  Long register(MemberDTO memberDTO);

  PageResultDTO<MemberDTO, Member> getList(PageRequestDTO pageRequestDTO);

  MemberDTO read(Long mno);

  Long modify(MemberDTO memberDTO);

  Long remove(Long mno);

  MemberDTO checkLogin(String email, String pw);

  default Member dtoToEntity(MemberDTO memberDTO) {
    Member member = Member.builder()
        .mid(memberDTO.getMid())
        .email(memberDTO.getEmail())
        .pw(memberDTO.getPw())
        .nickname(memberDTO.getNickname())
        .build();
    return member;
  }

  default MemberDTO entityToDto(Member member) {
    MemberDTO memberDTO = MemberDTO.builder()
        .mid(member.getMid())
        .email(member.getEmail())
        .pw(member.getPw())
        .nickname(member.getNickname())
        .regDate(member.getRegDate())
        .modDate(member.getModDate())
        .build();
    return memberDTO;
  }
}
