package com.example.ex76.service;

import com.example.ex76.dto.MemberDTO;
import com.example.ex76.dto.PageRequestDTO;
import com.example.ex76.dto.PageResultDTO;
import com.example.ex76.entity.Member;
import com.example.ex76.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.function.Function;

@Service
@Log4j2
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
  private final MemberRepository memberRepository;

  @Override
  public Long register(MemberDTO memberDTO) {
    return memberRepository.save(dtoToEntity(memberDTO)).getMid();
  }

  @Override
  public PageResultDTO<MemberDTO, Member> getList(PageRequestDTO pageRequestDTO) {
    Pageable pageable = pageRequestDTO.getPageable(Sort.by("mid").descending());
    Page<Member> result = memberRepository.findAll(pageable);

    Function<Member, MemberDTO> fn = new Function<Member, MemberDTO>() {
      @Override
      public MemberDTO apply(Member member) {
        return entityToDto(member);
      }
    };
    return new PageResultDTO<>(result, fn);
  }

  @Override
  public MemberDTO read(Long mid) {
    Optional<Member> result = memberRepository.findById(mid);
    if (result.isPresent()) {
      return entityToDto(result.get());
    }
    return null;
  }

  @Override
  public Long modify(MemberDTO memberDTO) {
    Long result = null;
    Optional<Member> find = memberRepository.findById(memberDTO.getMid());
    if (find.isPresent()) {
      Member member = find.get();
      member.changePw(memberDTO.getPw());
      member.changeNickname(memberDTO.getNickname());
      memberRepository.save(member);
      result = member.getMid();
    }
    return result;
  }

  @Override
  public Long remove(Long mid) {
    try {
      memberRepository.deleteById(mid);
    } catch (Exception e) { // 만약 삭제 실패 시 null 출력
      return null;
    }
    return mid;
  }

  @Override
  public MemberDTO checkLogin(String email, String pw) {
    Optional<Member> result = memberRepository.findMemberByEmail(email);
    if (result.isPresent()) {
      Member member = result.get();
      if (member.getPw().equals(pw)) {
        return entityToDto(member);
      }
    }
    return null;
  }
}
