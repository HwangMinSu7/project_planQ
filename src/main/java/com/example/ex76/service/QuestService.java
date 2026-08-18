package com.example.ex76.service;

import com.example.ex76.dto.QuestCompletionResult;
import com.example.ex76.dto.QuestDashboardDTO;
import com.example.ex76.entity.*;
import com.example.ex76.exception.NotFoundException;
import com.example.ex76.repository.ClubMemberRepository;
import com.example.ex76.repository.DailyQuestRepository;
import com.example.ex76.repository.MissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Transactional
public class QuestService {
  private final ClubMemberRepository memberRepository;
  private final MissionRepository missionRepository;
  private final DailyQuestRepository dailyQuestRepository;
  private final QuestImageStorage imageStorage;
  private final BadgeService badgeService;

  public QuestDashboardDTO getDashboard(String email) {
    ClubMember member = getMember(email);
    member.initializeRewardProfile();
    DailyQuest today = getOrAssignToday(member);
    List<DailyQuest> history = dailyQuestRepository.findTop8ByMember_EmailOrderByQuestDateDesc(email);
    List<DailyQuest> communityProofs = dailyQuestRepository
        .findTop8ByStatusOrderByCompletedAtDesc(QuestStatus.COMPLETED);
    return new QuestDashboardDTO(member, today, history, communityProofs);
  }

  public void reroll(String email) {
    DailyQuest quest = findToday(email);
    List<Mission> candidates = missionRepository.findByActiveTrueAndWebFriendlyTrue().stream()
        .filter(mission -> !mission.getId().equals(quest.getMission().getId()))
        .toList();
    if (candidates.isEmpty()) throw new IllegalStateException("교체할 다른 미션이 없습니다.");
    quest.reroll(candidates.get(ThreadLocalRandom.current().nextInt(candidates.size())));
  }

  public QuestCompletionResult complete(String email, String note, MultipartFile proofImage) {
    DailyQuest quest = findToday(email);
    if ((note == null || note.isBlank()) && (proofImage == null || proofImage.isEmpty())) {
      throw new IllegalArgumentException("한 줄 기록이나 인증 사진 중 하나는 남겨 주세요.");
    }
    if (note != null && note.trim().length() > 500) {
      throw new IllegalArgumentException("한 줄 기록은 500자 이하로 입력해 주세요.");
    }

    String savedPath = imageStorage.save(proofImage);
    quest.complete(note, savedPath);
    ClubMember member = quest.getMember();
    int earned = member.rewardQuest(LocalDate.now(), quest.getMission().getPoints());
    badgeService.evaluate(email);
    return new QuestCompletionResult(earned, member.getRewardPoints(),
        member.getCurrentStreak(), member.getLevel());
  }

  @Transactional(readOnly = true)
  public QuestImageStorage.StoredImage getProofImage(Long questId) {
    DailyQuest quest = dailyQuestRepository.findWithDetailById(questId)
        .orElseThrow(() -> new NotFoundException("퀘스트 기록을 찾을 수 없습니다."));
    return imageStorage.load(quest.getProofImagePath());
  }

  private DailyQuest findToday(String email) {
    return dailyQuestRepository.findByMember_EmailAndQuestDate(email, LocalDate.now())
        .orElseGet(() -> getOrAssignToday(getMember(email)));
  }

  private DailyQuest getOrAssignToday(ClubMember member) {
    return dailyQuestRepository.findByMember_EmailAndQuestDate(member.getEmail(), LocalDate.now())
        .orElseGet(() -> dailyQuestRepository.save(DailyQuest.builder()
            .member(member)
            .mission(randomMission())
            .questDate(LocalDate.now())
            .build()));
  }

  private Mission randomMission() {
    List<Mission> missions = missionRepository.findByActiveTrueAndWebFriendlyTrue();
    if (missions.isEmpty()) throw new IllegalStateException("등록된 미션이 없습니다.");
    return missions.get(ThreadLocalRandom.current().nextInt(missions.size()));
  }

  private ClubMember getMember(String email) {
    return memberRepository.findById(email)
        .orElseThrow(() -> new IllegalArgumentException("로그인 회원을 찾을 수 없습니다."));
  }
}
