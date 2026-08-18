package com.example.ex76.service;

import com.example.ex76.dto.ClubMemberDTO;

public interface ClubMemberService {
  String register(ClubMemberDTO clubMemberDTO);
  boolean exists(String email);
  ClubMemberDTO getProfile(String loginEmail);
  String modify(String loginEmail, ClubMemberDTO clubMemberDTO);
  void userAccess();
  void managerAccess();
  void adminAccess();
  void selfAccess(String username);
}
