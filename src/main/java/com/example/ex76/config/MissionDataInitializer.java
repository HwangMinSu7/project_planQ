package com.example.ex76.config;

import com.example.ex76.entity.Mission;
import com.example.ex76.entity.MissionCategory;
import com.example.ex76.repository.MissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MissionDataInitializer implements CommandLineRunner {
  private final MissionRepository missionRepository;

  @Override
  public void run(String... args) {
    List<Mission> webMissions = List.of(
        mission("브라우저 탭 세 개 정리하기", "열어 둔 탭 중 필요 없는 세 개를 닫고 한 줄로 인증해 보세요.", MissionCategory.DAILY),
        mission("바탕화면 파일 하나 정리하기", "바탕화면의 파일 하나를 폴더로 옮기거나 삭제해 보세요.", MissionCategory.DAILY),
        mission("메모장에 오늘 할 일 세 개 쓰기", "컴퓨터 메모장에 오늘 끝낼 작은 일 세 가지를 적어 보세요.", MissionCategory.DAILY),
        mission("즐겨찾기 하나 정리하기", "더 이상 보지 않는 즐겨찾기 하나를 지우거나 새 페이지 하나를 저장해 보세요.", MissionCategory.REFRESH),
        mission("키보드 주변 3분 정리", "컴퓨터 앞에서 바로 할 수 있게 키보드 주변 물건을 간단히 정리해 보세요.", MissionCategory.HEALTH),
        mission("오늘 기분을 한 문장으로 저장하기", "메모장에 지금 기분을 한 문장으로 적고 제목을 붙여 보세요.", MissionCategory.CREATIVE),
        mission("다운로드 폴더 파일 하나 정리하기", "다운로드 폴더에서 필요 없는 파일 하나를 정리해 보세요.", MissionCategory.DAILY),
        mission("좋아하는 사진 한 장 폴더에 모으기", "컴퓨터에 있는 좋아하는 사진 한 장을 새 폴더에 모아 보세요.", MissionCategory.REFRESH)
    );
    webMissions.stream()
        .filter(mission -> !missionRepository.existsByTitle(mission.getTitle()))
        .forEach(missionRepository::save);
  }

  private Mission mission(String title, String description, MissionCategory category) {
    return Mission.builder().title(title).description(description).category(category)
        .points(100).active(true).webFriendly(true).build();
  }
}
