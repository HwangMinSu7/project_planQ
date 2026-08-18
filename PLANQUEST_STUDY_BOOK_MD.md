# PLAN QUEST 프로젝트 공부책

이 문서는 PLAN QUEST를 단순히 실행하는 방법만 적은 설명서가 아니다. 지금까지 구현한 기능을 직접 이해하고, 코드의 흐름을 따라가고, 나중에 자신의 말로 설명할 수 있도록 만든 학습용 문서다.

기존의 `PLANQUEST_MD.md`가 작업 기록과 기능 위치를 빠르게 찾는 문서라면, 이 문서는 처음부터 순서대로 공부하는 교재에 가깝다.

---

## 1. 먼저 알아둘 것

이 프로젝트는 다음 내용을 한 번에 연습할 수 있는 Spring Boot 웹 프로젝트다.

- Spring MVC 요청 처리
- Thymeleaf 서버 렌더링
- Spring Data JPA와 엔티티 관계
- Spring Security 로그인과 권한
- MySQL 데이터 저장
- 파일 업로드와 이미지 출력
- 트랜잭션과 더티 체킹
- 포인트 거래 흐름
- 스케줄러를 이용한 자동 만료
- WebSocket과 STOMP 실시간 채팅
- JUnit 통합 테스트

모든 코드를 한 번에 외우려고 하면 어렵다. 다음 순서로 반복해서 보는 것이 좋다.

```text
화면에서 버튼 확인
  → Controller의 URL 찾기
  → Service 메서드 찾기
  → Entity 상태가 어떻게 바뀌는지 확인
  → Repository가 어떤 데이터를 조회하는지 확인
  → Test에서 실제 사용 예 확인
```

이 순서가 이 프로젝트를 공부할 때 가장 중요한 기준이다.

---

## 2. 프로젝트 한 문장 소개

PLAN QUEST는 하루에 하나의 간단한 퀘스트를 수행하고, 커뮤니티에서 글과 모임을 만들며, 회원끼리 포인트를 걸고 도움 미션을 주고받을 수 있는 웹 서비스다.

포트폴리오에서는 다음과 같이 소개하면 자연스럽다.

> 매일 하나의 작은 목표를 수행하도록 도와주는 커뮤니티 서비스입니다. 일일 퀘스트, 인증 기록, 포인트와 뱃지, 게시판과 모임, 사용자 간 도움 미션 및 실시간 채팅을 구현했습니다.

---

## 3. 지금까지 구현한 기능 전체 목록

### 첫 화면

- 회원가입과 로그인 이동 버튼
- PLAN QUEST의 사용 예시를 보여주는 정적 퀘스트 카드
- `CLEAR +100P`는 실제 버튼이 아닌 디자인용 완료 표시
- 실제 퀘스트 수행과 완료는 로그인 후 오늘의 퀘스트 화면에서 처리

### 계정과 보안

- 아이디 기반 회원가입과 로그인
- 아이디에 영문자와 숫자를 모두 포함하도록 검사
- 영문자와 숫자가 포함된 이메일 형식 아이디도 허용
- 영문 대소문자를 같은 아이디로 처리
- 회원가입 비밀번호 확인
- BCrypt 비밀번호 암호화
- 현재 비밀번호 확인 후 새 비밀번호 변경
- 일반 회원과 운영자 권한 구분
- `admin / 1234` 개발용 운영자 계정 초기화
- 오이데 계정에 운영자 권한 부여
- 운영자 전용 신고·문의 메뉴

### 오늘의 퀘스트

- 회원별 오늘의 퀘스트 자동 배정
- 컴퓨터나 노트북으로 쉽게 수행할 수 있는 미션만 배정
- 하루 한 번 다른 미션으로 교체
- 글 또는 사진으로 완료 인증
- 완료 포인트와 연속 달성 보너스
- 포인트에 따른 레벨 계산
- 최근 완료 내역과 다른 회원의 인증 기록
- 주간 달성 현황

### 커뮤니티

- 일반글, 질문글, 모임글, 공지사항
- 운영자에게만 보이는 빨간 `공지 쓰기` 전용 버튼
- 게시글 작성·조회·검색·수정·삭제
- 게시글 사진 첨부·교체·삭제
- 카드 보기와 목록 보기 전환
- 댓글 작성·삭제
- 게시글과 댓글 좋아요
- 게시글 작성자의 댓글 하나 상단 고정
- 운영자의 게시글 상단 고정
- 고정 게시글 드래그 순서 변경
- 공지사항 항상 상단 배치
- 모임 날짜·장소·정원 설정
- 모임 참가와 참가 취소
- 게시글·댓글 신고

### 마이페이지와 뱃지

- 프로필 사진 업로드
- 닉네임, 아이디, 레벨, 포인트 표시
- 레벨 진행률 표시
- 완료 퀘스트, 연속 달성, 작성 글, 댓글, 모임 통계
- 이번 주 달성 현황
- 카테고리별 완료 통계
- 12종 뱃지 자동 지급
- 보유 뱃지 중 대표 뱃지 선택

### 도움 미션

- 회원이 필요한 미션을 직접 등록
- 미션 작성 시 자신의 포인트를 보상으로 걸기
- 운영자 승인 또는 반려
- 반려 시 작성자에게 포인트 반환
- 다른 회원의 미션 수락 요청
- 작성자가 10분 안에 수행자 확정 또는 거절
- 10분이 지나면 수락 요청 자동 취소
- 확정된 작성자와 수행자의 실시간 채팅
- 채팅 내역 DB 저장
- 양쪽 모두 미션 완료 확인
- 운영자가 최종 승인하면 수행자에게 포인트 지급
- 중복 지급 방지

### 알림과 운영

- 상단 알림 벨과 읽지 않은 알림 개수
- 수락 요청, 수락 결과, 완료 확인, 채팅 알림
- 알림 목록과 읽음 처리
- 게시글·댓글·미션 제안 신고
- 일반 문의 작성
- 운영자 신고 목록 조회와 처리 상태 변경

---

## 4. 기술 구성

| 구분 | 사용 기술 | 프로젝트에서 하는 일 |
|---|---|---|
| 언어 | Java 17 | 백엔드 코드 작성 |
| 프레임워크 | Spring Boot 4.0.6 | 애플리케이션 실행과 설정 |
| 웹 | Spring MVC | URL 요청과 응답 처리 |
| 화면 | Thymeleaf | 서버 데이터를 HTML에 출력 |
| 보안 | Spring Security | 로그인, 세션, 역할별 권한 검사 |
| 데이터 | Spring Data JPA | 엔티티 저장과 조회 |
| DB | MySQL | 회원, 게시글, 퀘스트, 채팅 저장 |
| 실시간 통신 | WebSocket + STOMP | 미션 채팅 전송과 구독 |
| 자동 처리 | Spring Scheduling | 10분 지난 수락 요청 만료 |
| 편의 기능 | Lombok | 생성자, Getter, Builder 생성 |
| 테스트 | JUnit + Spring Boot Test | 도메인 규칙과 서비스 흐름 검증 |

현재 서버 설정은 다음과 같다.

```text
포트: 8081
컨텍스트 경로: /ex76
접속 주소: http://localhost:8081/ex76
DB 기본값: jdbc:mysql://localhost:3306/db7
업로드 기본 폴더: C:/upload
```

---

## 5. 전체 구조 이해하기

이 프로젝트는 기본적으로 다음 구조를 사용한다.

```text
브라우저
  ↓ HTTP 요청 또는 WebSocket 메시지
Controller
  ↓ 필요한 작업 요청
Service
  ↓ 조회·저장
Repository
  ↓ SQL 변환
MySQL
```

각 계층의 책임은 다음과 같다.

### Controller

Controller는 URL을 받고 화면 또는 리다이렉트 주소를 반환한다.

예를 들어 사용자가 커뮤니티 글 작성 버튼을 누르면 `CommunityController`가 폼 값을 받고 로그인 회원 아이디와 함께 `CommunityService.create()`를 호출한다.

Controller에 DB 처리 규칙을 길게 작성하지 않는 이유는 같은 기능을 테스트나 다른 화면에서도 재사용하기 어렵기 때문이다.

### Service

Service는 실제 업무 규칙을 처리한다.

예시는 다음과 같다.

- 작성자만 게시글을 수정할 수 있는지 검사
- 포인트가 충분한지 검사
- 수락 요청이 10분을 넘겼는지 검사
- 두 사람이 모두 완료했는지 검사
- 파일을 저장하고 엔티티에 경로 기록

### Repository

Repository는 DB에 데이터를 저장하고 필요한 조건으로 조회한다.

Spring Data JPA 메서드 이름만으로도 간단한 쿼리를 만들 수 있다.

```java
findByStatusOrderByIdDesc(status, pageable)
```

이 이름은 상태로 검색하고 ID 내림차순으로 정렬하라는 의미다.

### Entity

Entity는 DB 테이블과 연결되는 객체다. 이 프로젝트에서는 단순히 필드만 보관하지 않고 자신의 상태를 바꾸는 메서드도 가진다.

```java
quest.complete(note, savedPath);
member.rewardQuest(LocalDate.now(), points);
suggestion.confirmAcceptance(email);
```

이렇게 하면 완료 규칙이 여러 Service로 흩어지지 않는다.

### DTO와 Form

화면 입력과 화면 출력에 엔티티를 그대로 사용하지 않을 때 DTO를 사용한다.

- `BoardPostForm`: 게시글 작성 화면 입력
- `ClubMemberDTO`: 회원가입과 회원정보 수정 입력
- `BoardDetailDTO`: 게시글 상세 화면에 필요한 여러 데이터 묶음
- `MissionChatRequest`: 채팅으로 들어온 메시지
- `MissionChatMessageDTO`: 채팅으로 내보낼 메시지

---

## 6. 중요한 데이터 관계

전체 테이블을 모두 외울 필요는 없다. 먼저 중심이 되는 관계를 이해하자.

```text
ClubMember
 ├─ DailyQuest ─ Mission
 ├─ BoardPost ─ BoardComment
 │            ├─ PostLike
 │            ├─ CommentLike
 │            └─ MeetupParticipant
 ├─ MemberBadge ─ Badge
 ├─ MissionSuggestion
 │    ├─ applicant
 │    ├─ performer
 │    └─ MissionSuggestionComment
 ├─ UserNotification
 └─ UserReport
```

### `@ManyToOne`

여러 게시글이 한 명의 작성자를 가질 수 있으므로 `BoardPost`에서 `ClubMember`는 `@ManyToOne`이다.

```java
@ManyToOne(fetch = FetchType.LAZY)
private ClubMember author;
```

### `@OneToMany`

게시글 하나는 댓글 여러 개를 가질 수 있다.

```java
@OneToMany(mappedBy = "post", cascade = CascadeType.ALL,
    orphanRemoval = true)
private List<BoardComment> comments;
```

- `mappedBy`: 관계의 실제 외래키는 댓글 쪽에 있다는 뜻
- `cascade = ALL`: 게시글 작업이 자식 엔티티에도 전파될 수 있음
- `orphanRemoval = true`: 관계에서 제거된 자식 데이터를 삭제

### `FetchType.LAZY`

연관된 데이터를 항상 한꺼번에 가져오지 않고 실제 필요할 때 조회한다. 데이터가 많아졌을 때 불필요한 조회를 줄이는 데 도움이 된다.

다만 트랜잭션 밖에서 지연 로딩 데이터를 사용하거나 목록에서 연관 데이터를 반복 조회하면 문제가 생길 수 있다. 그래서 일부 Repository에는 `@EntityGraph`를 사용해 필요한 관계를 함께 가져오도록 했다.

### `@Version`

`ClubMember`와 `BoardPost`에는 `@Version` 필드가 있다. 두 요청이 같은 데이터를 동시에 수정했을 때 나중 요청이 앞선 변경을 조용히 덮어쓰는 일을 줄이는 낙관적 락 기능이다.

---

## 7. 회원가입과 로그인 공부하기

### 전체 흐름

```text
register.html
  → POST /auth/register
  → AuthController.register()
  → ClubMemberServiceImpl.register()
  → BCrypt 암호화
  → ClubMemberRepository.save()
  → 로그인 화면으로 이동
```

### 아이디 규칙

화면에서는 아이디라고 표시하지만 기존 엔티티의 기본키 필드 이름은 `email`이다. 즉, 현재 코드에서 `email`은 실제 의미상 로그인 아이디다. 기존 코드를 크게 바꾸지 않기 위해 DB 필드 이름을 유지한 것이다.

허용되는 형태는 다음과 같다.

```text
plan1234            허용
PLAN1234            소문자 plan1234로 저장
plan1234@test.com   허용
plan                숫자가 없어서 거절
123456              영문자가 없어서 거절
```

검사는 브라우저 입력 패턴과 Service에서 함께 수행한다. 브라우저 검사는 사용자가 바로 오류를 보게 하고, Service 검사는 개발자 도구나 직접 요청으로 검사를 우회하는 것을 막는다.

### 비밀번호 저장

비밀번호는 원문으로 저장하지 않고 BCrypt 결과를 저장한다.

```java
passwordEncoder.encode(dto.getPassword())
```

로그인 또는 비밀번호 변경 때는 원문을 복호화하지 않는다.

```java
passwordEncoder.matches(inputPassword, savedPassword)
```

입력값을 같은 방식으로 비교하는 것이다.

### 인증과 인가의 차이

- 인증(Authentication): 지금 로그인한 사람이 누구인지 확인
- 인가(Authorization): 로그인한 사람이 이 기능을 사용할 권한이 있는지 확인

`SecurityConfig`는 공개 URL과 로그인 필요 URL을 나눈다. 서비스의 `@PreAuthorize("hasRole('ADMIN')")`는 운영자 기능을 한 번 더 보호한다.

운영자 버튼을 화면에서 숨기는 것만으로는 보안이 되지 않는다. 사용자가 URL을 직접 호출할 수 있기 때문에 서버 Service에서도 권한을 검사해야 한다.

### 꼭 찾아볼 파일

```text
config/SecurityConfig.java
security/service/ClubUserDetailsService.java
service/ClubMemberServiceImpl.java
controller/AuthController.java
templates/auth/register.html
templates/auth/login.html
templates/auth/modify.html
```

---

## 8. 오늘의 퀘스트 공부하기

### 조회할 때 자동 배정되는 이유

사용자가 오늘의 퀘스트 화면을 열면 `QuestService.getDashboard()`가 실행된다. 오늘 날짜의 퀘스트가 없으면 `getOrAssignToday()`가 새 `DailyQuest`를 만든다.

```text
오늘 퀘스트 있음 → 기존 데이터 반환
오늘 퀘스트 없음 → 활성화된 웹 친화 미션 중 하나 선택 → 저장 → 반환
```

DB에는 회원과 날짜 조합에 유니크 제약이 있다.

```java
@UniqueConstraint(columnNames = {"member_email", "quest_date"})
```

같은 회원에게 같은 날짜의 퀘스트가 두 개 생기는 것을 DB 수준에서도 막는다.

### 미션 교체

교체는 하루에 한 번만 가능하며 완료된 퀘스트는 바꿀 수 없다. 이 규칙은 `DailyQuest.reroll()` 안에 있다.

### 완료와 보상

퀘스트 완료 요청의 핵심 순서는 다음과 같다.

```text
입력 검사
  → 인증 이미지 저장
  → DailyQuest 완료 상태 변경
  → 회원 포인트와 연속 달성 계산
  → 뱃지 조건 검사
  → 결과 DTO 반환
```

연속 보너스는 하루 연속으로 수행했을 때 증가하며 최대 100P까지 붙는다.

```java
int streakBonus = Math.min((currentStreak - 1) * 10, 100);
```

레벨은 현재 포인트 500점마다 한 단계 올라간다.

```java
level = (rewardPoints / 500) + 1;
```

### 여기서 배울 수 있는 것

- 날짜를 기준으로 한 데이터 생성
- 유니크 제약으로 중복 방지
- 엔티티 내부의 도메인 규칙
- 파일과 DB 작업을 한 Service에서 연결
- 완료 후 연관 기능인 뱃지 평가 호출

---

## 9. 커뮤니티 공부하기

### 게시글 생성 흐름

```text
community/form.html
  → POST /community
  → CommunityController.create()
  → CommunityService.validate()
  → CommunityImageStorage.save()
  → BoardPostRepository.save()
  → /community/{id}로 이동
```

### 작성자와 운영자의 권한 차이

현재 규칙은 다음과 같다.

| 작업 | 작성자 | 운영자 | 일반 회원 |
|---|---:|---:|---:|
| 자신의 글 수정 | 가능 | 작성자가 아니면 불가 | 불가 |
| 글 삭제 | 가능 | 가능 | 불가 |
| 공지 작성 | 운영자일 때 가능 | 가능 | 불가 |
| 게시글 고정 | 불가 | 가능 | 불가 |
| 댓글 고정 | 자신의 글에서 가능 | 가능 | 불가 |
| 댓글 삭제 | 자신의 댓글 가능 | 가능 | 자신의 댓글만 가능 |

운영자라도 다른 사람의 게시글을 수정할 수 없도록 `getOwnedPost()`를 사용한다. 삭제는 운영상 필요하므로 `getManageablePost()`에서 운영자도 허용한다.

### 좋아요 토글

좋아요는 누르면 생성되고 다시 누르면 삭제된다.

```text
좋아요 데이터 존재
  ├─ 예: Like 삭제 + 카운트 감소
  └─ 아니오: Like 생성 + 카운트 증가
```

별도의 `PostLike` 엔티티를 두는 이유는 누가 좋아요를 눌렀는지 기록하고 한 회원의 중복 좋아요를 막기 위해서다. `likeCount`는 목록에서 매번 전체 좋아요를 세지 않도록 게시글에도 저장한 값이다.

### 모임 참가

참가할 때 Service는 다음을 확인한다.

1. 모임 카테고리인가?
2. 모임 시간이 지나지 않았는가?
3. 이미 참가하지 않았는가?
4. 정원이 남아 있는가?

통과하면 `MeetupParticipant`를 저장하고 게시글의 참가자 수를 증가시킨다. 참가 취소 때는 반대로 처리한다.

### 공지와 고정 순서

공지사항은 작성과 동시에 고정된다. 일반 게시글도 운영자가 고정할 수 있으며 `pinOrder` 값으로 순서를 정한다. 화면에서 드래그하면 게시글 ID 배열을 서버에 보내고, 서버는 전달된 ID가 현재 고정글 전체와 일치하는지 확인한 뒤 순서를 변경한다.

이 검사가 없으면 사용자가 임의의 다른 게시글 ID를 섞어서 순서를 바꿀 수 있다.

커뮤니티 목록의 빨간 `공지 쓰기` 버튼은 운영자에게만 보인다. 주소는 `/community/new?category=NOTICE`이며 `CommunityController.createForm()`이 요청값을 받아 작성 폼의 카테고리를 공지로 미리 선택한다.

여기서 중요한 것은 화면에서 버튼을 숨기는 것만으로 끝내지 않았다는 점이다.

```text
1차: Thymeleaf의 sec:authorize로 일반 회원에게 버튼 숨김
2차: Controller에서 현재 회원이 작성 가능한 카테고리인지 확인
3차: Service에서 NOTICE 작성 시 ADMIN 역할 다시 확인
```

이처럼 화면은 사용 편의를 담당하고 Controller와 Service는 조작된 요청을 방어한다. 운영자는 일반 `새 글 쓰기`에서 직접 공지를 선택할 수도 있고, 전용 버튼을 이용해 공지가 선택된 폼으로 바로 들어갈 수도 있다.

### 이미지 업로드

DB에는 이미지 바이너리를 직접 넣지 않고 저장된 파일 경로만 기록한다.

```text
MultipartFile
  → 확장자와 실제 이미지 형식 검사
  → 안전한 파일 이름 생성
  → 업로드 폴더 저장
  → 상대 경로를 BoardPost.imagePath에 저장
```

글을 수정하며 이미지를 교체하면 새 파일을 먼저 저장하고 기존 파일을 삭제한다. 이미지 제거를 선택하면 DB 경로를 비우고 실제 파일도 삭제한다.

### 카드 보기와 목록 보기

이 기능은 DB 구조를 바꾸지 않고 같은 게시글 데이터를 CSS와 JavaScript로 다르게 표시한다. 백엔드 기능과 프론트 화면 상태가 구분되는 좋은 예다.

---

## 10. 마이페이지와 뱃지 공부하기

### 여러 Repository를 합쳐 하나의 화면 만들기

마이페이지는 한 테이블만 조회해서 만들 수 없다. `MyPageService.getMyPage()`가 다음 데이터를 모아 `MyPageDTO` 하나로 반환한다.

- 회원 기본 정보
- 완료 퀘스트 수
- 작성 글과 댓글 수
- 참가 모임 수
- 이번 주 7일 상태
- 카테고리별 완료 수
- 최근 퀘스트와 게시글
- 보유 뱃지와 대표 뱃지

이것을 화면 조립용 DTO라고 이해하면 된다.

### 대표 뱃지 권한 검사

화면에서 뱃지를 선택했더라도 Service는 실제 보유한 뱃지인지 다시 확인한다.

```java
existsByMember_EmailAndBadge_Code(email, badgeCode)
```

보유하지 않은 뱃지 코드를 직접 전송해 대표 뱃지로 설정하는 것을 막는다.

### 12개 뱃지 조건

| 코드 | 조건 |
|---|---|
| `FIRST_QUEST` | 퀘스트 1개 완료 |
| `STREAK_3` | 3일 연속 달성 |
| `STREAK_7` | 7일 연속 달성 |
| `COMMENT_10` | 댓글 10개 작성 |
| `FIRST_MEETUP` | 모임 1개 개설 |
| `JOIN_3` | 모임 3회 참가 |
| `QUEST_10` | 퀘스트 10개 완료 |
| `QUEST_30` | 퀘스트 30개 완료 |
| `POST_5` | 게시글 5개 작성 |
| `LIKE_10` | 작성 글에서 좋아요 10개 획득 |
| `ALL_CATEGORY` | 모든 미션 카테고리 완료 |
| `WEEK_CLEAR` | 이번 주 7일 완료 |

`BadgeDataInitializer`는 뱃지 종류를 DB에 넣고, `BadgeService.evaluate()`는 회원 행동 뒤 조건을 계산해 아직 없는 뱃지만 지급한다.

---

## 11. 도움 미션과 포인트 거래 공부하기

이 기능은 현재 프로젝트에서 가장 많은 백엔드 개념이 연결된 부분이다.

### 전체 상태 흐름

```text
작성자가 미션 등록
  → 보상 포인트 즉시 차감(보관)
  → PENDING
     ├─ 운영자 반려 → REJECTED + 작성자에게 포인트 반환
     └─ 운영자 승인 → APPROVED
          → 다른 회원이 수락 요청(applicant)
          → 작성자가 10분 안에 확인
             ├─ 거절/시간 만료 → applicant 제거, 다시 모집
             └─ 수락 → performer 확정 + 채팅 개방
                  → 작성자 완료 확인
                  → 수행자 완료 확인
                  → 운영자 정산
                  → 수행자에게 보상 포인트 지급
```

### 포인트를 등록할 때 먼저 차감하는 이유

미션이 끝난 뒤 포인트를 차감하려고 하면 작성자가 그사이에 포인트를 모두 사용해 버릴 수 있다. 그래서 등록할 때 차감하여 보상 포인트를 확보한다. 실제 별도 지갑 테이블은 없고 `MissionSuggestion.bountyPoints`가 보관 중인 금액을 나타낸다.

이 방식은 간단한 프로젝트에서 에스크로와 비슷한 흐름을 연습하기 좋다.

### 왜 완료 버튼이 두 개 필요한가

수행자만 완료 버튼을 누르면 실제 요청자가 결과를 받았는지 알기 어렵다. 작성자만 누르게 하면 수행자가 일을 끝냈는데 작성자가 확인하지 않을 수 있다. 현재는 양쪽 모두 완료를 누른 뒤 운영자가 정산하도록 구성했다.

### 중복 지급 방지

`pointsSettled`가 이미 참이면 다시 정산할 수 없다.

```java
if (pointsSettled) {
    throw new IllegalStateException("이미 포인트를 지급했습니다.");
}
```

이런 필드는 결제나 쿠폰 지급처럼 같은 작업이 두 번 실행되면 안 되는 기능에서 중요하다. 이를 멱등성 관점의 방어라고 설명할 수 있다.

### 현재 구현의 범위

- 한 미션에는 동시에 한 명만 수락 요청 가능
- 작성자 확인 전에는 수행자가 아님
- 미션 게시글 삭제 기능은 아직 없음
- 승인된 미션이 끝까지 수행되지 않을 때 운영자가 강제 취소하거나 환불하는 기능은 아직 없음
- 실제 결제 시스템이 아니라 서비스 내부 포인트만 사용

한계를 숨기지 않고 설명하면 설계를 더 정확히 이해하고 있다는 인상을 준다.

---

## 12. 알림과 10분 자동 취소 공부하기

### 알림 저장 방식

알림은 `UserNotification` 엔티티로 DB에 저장한다.

주요 데이터는 다음과 같다.

- 받는 회원
- 알림 문장
- 이동할 URL
- 읽음 여부
- 생성 시각

`GlobalModelAdvice`는 Thymeleaf 화면이 열릴 때 읽지 않은 알림 수를 공통 Model에 넣는다. 그래서 모든 Controller가 같은 코드를 반복하지 않아도 레이아웃의 벨 숫자를 출력할 수 있다.

### 현재 알림의 정확한 동작

알림 데이터는 바로 DB에 저장된다. 다만 벨 숫자 자체를 WebSocket으로 밀어주는 구조는 아니므로 이미 열려 있는 다른 페이지에서는 이동하거나 새로고침했을 때 숫자가 갱신된다. 실시간으로 전달되는 것은 미션 채팅이다.

### 스케줄러

메인 애플리케이션의 `@EnableScheduling`이 스케줄러를 켠다.

```java
@Scheduled(fixedDelay = 60_000)
```

이 메서드는 이전 실행이 끝난 후 60초가 지나면 다시 실행된다. 실행할 때 현재 시각보다 10분 이상 오래된 수락 요청을 조회해 신청자를 제거한다.

상세 페이지에 들어갈 때도 만료 시간을 확인한다. 스케줄러 실행 사이의 약간의 시간 차이가 있어도 사용자가 상세 페이지를 열면 만료된 요청을 즉시 정리할 수 있다.

중요한 점은 미션 전체가 삭제되거나 반려되는 것이 아니라 `applicant`와 `acceptRequestedAt`만 초기화된다는 것이다.

---

## 13. WebSocket 실시간 채팅 공부하기

### HTTP와 WebSocket 차이

일반 HTTP는 브라우저가 요청해야 서버가 응답한다.

```text
브라우저 → 요청 → 서버
브라우저 ← 응답 ← 서버
```

WebSocket은 처음 연결한 뒤 연결을 유지한다. 서버가 새 메시지를 받으면 연결된 다른 사용자에게 바로 보낼 수 있다.

```text
작성자 브라우저 ↔ WebSocket 서버 ↔ 수행자 브라우저
```

### STOMP를 사용하는 이유

WebSocket 자체는 메시지 형식과 주소 규칙이 단순하다. STOMP를 사용하면 메시지를 어디로 보낼지와 어떤 주소를 구독할지 구분할 수 있다.

```text
연결: /ws-mission
전송: /app/suggestions/{id}/chat
구독: /topic/suggestions/{id}
```

- `/app`: 서버 Controller가 처리해야 하는 메시지
- `/topic`: 여러 구독자에게 방송되는 메시지

### 메시지 한 개가 이동하는 순서

```text
1. 브라우저가 /app/suggestions/3/chat으로 JSON 전송
2. WebSocketConfig의 ChannelInterceptor가 권한 검사
3. MissionChatController.chat() 호출
4. MissionSuggestionService.addChatMessage() 호출
5. DB에 MissionSuggestionComment 저장
6. MissionChatMessageDTO 생성
7. /topic/suggestions/3 구독자에게 방송
8. 브라우저 JavaScript가 새 말풍선 추가
```

### 채팅을 DB에 저장하는 이유

WebSocket으로 방송만 하면 그 순간 접속 중인 사람만 메시지를 볼 수 있다. DB에 먼저 저장하면 다음과 같은 장점이 있다.

- 새로고침해도 이전 대화가 보임
- 나중에 접속한 참여자도 과거 대화를 확인
- 운영상 기록 확인 가능

### WebSocket 권한 검사

화면에서 채팅창을 숨겨도 사용자가 직접 STOMP 주소를 구독할 수 있다. `ChannelInterceptor`는 `SEND`와 `SUBSCRIBE` 프레임을 가로채 다음 조건을 검사한다.

```text
수행자가 확정되어 있고
현재 회원이 작성자 또는 수행자 또는 운영자인가?
```

조건을 통과하지 못하면 `AccessDeniedException`을 발생시킨다.

### 브라우저 코드에서 확인할 것

`suggestion/detail.html` 아래쪽 JavaScript에서 다음 순서로 찾는다.

1. `StompJs.Client` 생성
2. `brokerURL`로 WebSocket 주소 설정
3. `onConnect`에서 토픽 구독
4. 폼 제출 때 `client.publish()` 실행
5. 받은 JSON을 `appendMessage()`로 화면에 추가

메시지 내용은 HTML 문자열로 바로 넣지 않고 `textContent`를 사용한다. 사용자가 `<script>` 같은 문장을 보내도 HTML 코드로 실행되지 않게 하는 XSS 방어에 도움이 된다.

### 새 의존성을 추가했을 때 생길 수 있는 오류

`build.gradle`에 WebSocket starter를 추가한 뒤 IntelliJ가 Gradle을 다시 읽지 않으면 다음 오류가 날 수 있다.

```text
WebSocketMessageBrokerConfigurer.class ... does not exist
```

해결 순서:

1. 서버 완전 종료
2. IntelliJ의 Reload All Gradle Projects
3. Gradle 로딩이 끝난 뒤 다시 실행
4. 계속 안 되면 `./gradlew clean bootRun` 실행

이것은 포트나 DB 오류가 아니라 실행 클래스패스에 새 라이브러리가 반영되지 않은 문제다.

---

## 14. 신고와 운영자 기능 공부하기

신고는 게시글, 댓글, 미션 제안, 일반 문의를 하나의 `UserReport` 구조로 저장한다.

`ReportTargetType`으로 대상 종류를 구분하고 `targetId`에 실제 대상 번호를 저장한다.

장점은 신고 종류마다 별도 테이블과 화면을 만들지 않아도 된다는 것이다. 대신 대상이 정말 존재하는지 Service의 `resolveTargetTitle()`에서 확인한다.

운영자 조회와 처리에는 다음 어노테이션이 붙는다.

```java
@PreAuthorize("hasRole('ADMIN')")
```

화면 메뉴, Controller 접근, Service 업무 규칙 중 최소한 Service는 반드시 보호해야 한다.

---

## 15. 트랜잭션을 이해하는 방법

Service 클래스에 `@Transactional`이 붙으면 메서드 안의 여러 DB 변경을 하나의 작업 단위로 묶는다.

예를 들어 미션을 등록할 때 다음 두 작업이 있다.

1. 작성자 포인트 차감
2. 미션 제안 저장

중간에 예외가 발생하면 둘 다 취소되어야 한다. 포인트만 빠지고 미션은 저장되지 않으면 안 되기 때문이다.

### 더티 체킹

조회한 엔티티의 값을 트랜잭션 안에서 변경하면 `save()`를 다시 호출하지 않아도 커밋 시 JPA가 변경을 감지해 UPDATE한다.

```java
MissionSuggestion suggestion = getSuggestion(id);
suggestion.approve(reviewerEmail);
```

이 코드에 `suggestionRepository.save(suggestion)`가 없어도 상태가 저장되는 이유가 더티 체킹이다.

### `readOnly = true`

조회만 하는 메서드는 `@Transactional(readOnly = true)`로 표시했다. 코드의 의도를 분명하게 하고 불필요한 변경 감지를 줄이는 데 도움이 된다.

---

## 16. 예외 처리와 입력 검증

입력 검증은 크게 세 곳에서 볼 수 있다.

1. HTML 속성: 사용자가 바로 형식 오류를 확인
2. Form 또는 DTO 검증: 전달 데이터 형태 확인
3. Service 검증: 실제 업무 규칙 보호

현재 프로젝트는 Service에서 명시적인 조건문 검증을 많이 사용한다. 학생 프로젝트에서 흐름을 눈으로 따라가기 쉬운 방식이다.

`GlobalExceptionHandler`는 처리되지 않은 예외를 공통 오류 화면으로 연결한다. Controller에서 예상 가능한 오류는 `RedirectAttributes`의 flash 메시지로 이전 화면에 전달한다.

```java
ra.addFlashAttribute("error", e.getMessage());
return "redirect:/suggestions/new";
```

PRG(Post/Redirect/Get) 방식이므로 POST 처리 뒤 새로고침할 때 같은 등록 요청이 반복되는 것을 줄인다.

---

## 17. 테스트 코드 공부하기

현재 자동 테스트 결과는 다음과 같다.

```text
전체 41개
통과 40개
실패 0개
비활성화 1개
```

비활성화된 1개는 현재 PLAN QUEST에서 사용하지 않는 예전 Review 수업 코드 테스트다.

### 단위 테스트와 통합 테스트

- 도메인 단위 테스트: 엔티티 하나의 규칙을 빠르게 확인
- 통합 테스트: Spring Context와 Repository, Service를 연결해 실제 흐름 확인

### 테스트를 읽는 추천 순서

1. `OneQuestDomainTests`
   - 연속 보너스
   - 날짜가 끊겼을 때 연속 기록 초기화
   - 같은 날 중복 보상 방지
2. `ExtendedFeatureDomainTests`
   - 좋아요 수가 음수가 되지 않음
   - 미션 승인 중복 방지
3. `WorkflowAuditIntegrationTests`
   - 새 회원의 퀘스트 전체 흐름
   - 모임 정원
   - 다른 회원의 수정·삭제 차단
4. `CommunityModerationIntegrationTests`
   - 운영자 계정
   - 공지와 고정글
   - 댓글 고정과 댓글 좋아요
5. `AccountAndBountyIntegrationTests`
   - 회원가입과 비밀번호 변경
   - 포인트 미션 정산
   - 10분 자동 만료
   - 채팅 저장과 권한
6. `CommunityPostImageIntegrationTests`
   - 이미지 생성·교체·제거
   - 가짜 이미지 차단
7. `ReportIntegrationTests`
   - 신고 등록과 운영자 처리

### 테스트의 Given-When-Then

```text
Given: 테스트에 필요한 회원과 미션 준비
When:  수락이나 완료 같은 실제 메서드 실행
Then:  수행자, 포인트, 알림, DB 기록 확인
```

테스트를 읽을 때 메서드 이름만 보지 말고 준비 데이터와 마지막 검증 값을 연결해서 보자.

### 테스트 실행

PowerShell:

```powershell
.\gradlew.bat test
```

전체를 다시 실행하고 싶을 때:

```powershell
.\gradlew.bat test --rerun-tasks
```

HTML 결과:

```text
build/reports/tests/test/index.html
```

---

## 18. 코드를 공부하는 7일 순서

### 1일차: Spring MVC 한 바퀴

- `QuestController`의 GET 하나를 선택
- URL, Model 이름, 반환 템플릿 찾기
- 템플릿에서 `th:text`가 무엇을 출력하는지 확인
- 종이에 Controller → Service → Repository를 그리기

목표: 버튼을 눌렀을 때 어느 Java 파일로 가는지 찾을 수 있어야 한다.

### 2일차: 회원과 Security

- `SecurityConfig` 공개 URL 확인
- `ClubUserDetailsService`가 회원을 어떻게 읽는지 확인
- BCrypt의 `encode()`와 `matches()` 차이 정리
- USER와 ADMIN 권한이 어디서 검사되는지 검색

목표: 인증과 인가를 자신의 말로 구분할 수 있어야 한다.

### 3일차: JPA 관계와 CRUD

- `BoardPost`, `BoardComment`, `ClubMember` 관계 그리기
- 게시글 생성과 수정 흐름 따라가기
- `@ManyToOne`, `@OneToMany`, LAZY 의미 정리
- 더티 체킹 때문에 `save()`가 없는 부분 찾기

목표: 게시글 한 개가 저장될 때 어느 테이블에 어떤 값이 들어가는지 설명한다.

### 4일차: 업무 규칙

- 모임 참가 조건 네 가지 찾기
- 좋아요 토글 흐름 손으로 작성
- 퀘스트 연속 보너스를 숫자로 계산
- 작성자와 운영자의 권한 차이 정리

목표: 기능이 아니라 규칙을 Service에 두는 이유를 설명한다.

### 5일차: 포인트 미션

- PENDING → APPROVED 흐름 그리기
- 작성자, applicant, performer 차이 정리
- 포인트가 차감되고 지급되는 시점 확인
- `pointsSettled`가 필요한 이유 설명

목표: 포인트가 생성되거나 사라지지 않고 누구에게 이동하는지 추적한다.

### 6일차: 스케줄러와 WebSocket

- `@EnableScheduling`과 `@Scheduled` 찾기
- 10분 만료 쿼리 조건 읽기
- `/app`과 `/topic` 차이 정리
- 메시지 전송부터 DB 저장과 방송까지 순서 그리기
- `ChannelInterceptor`를 잠시 가렸다고 생각하고 생길 보안 문제 적기

목표: HTTP 요청과 실시간 메시지의 차이를 설명한다.

### 7일차: 테스트와 발표 연습

- 테스트 하나를 골라 Given-When-Then으로 나누기
- 조건 하나를 바꿔 실패하는 모습 확인 후 복구
- 프로젝트를 1분 동안 말로 소개
- 가장 어려웠던 기능과 해결 방법을 1분 동안 설명

목표: 코드를 보여주지 않고도 주요 설계를 설명한다.

---

## 19. 직접 해보면 좋은 연습 문제

정답을 바로 보지 말고 먼저 어떤 파일을 바꿔야 하는지 적어보자.

### 쉬움

1. 채팅 글자 제한을 600자에서 300자로 변경한다.
2. 게시글 페이지 크기를 10개에서 15개로 변경한다.
3. 알림 목록을 30개에서 20개로 변경한다.
4. 레벨업 기준을 500P에서 400P로 변경한다.
5. 수락 대기 화면 문구를 수정한다.

### 중간

1. 채팅 메시지에 보낸 시각을 `HH:mm` 형식으로 표시한다.
2. 사용자가 자신의 채팅 메시지를 구분할 수 있게 CSS 클래스를 추가한다.
3. 미션 작성자가 수락 요청을 거절할 때 간단한 사유를 입력하게 한다.
4. 읽은 알림과 읽지 않은 알림 필터를 만든다.
5. 모임 작성자가 참가자를 확인할 수 있는 별도 목록을 만든다.

### 도전

1. 이미 열린 화면의 알림 벨 숫자도 WebSocket으로 즉시 갱신한다.
2. 미션 취소와 포인트 환불 정책을 추가한다.
3. 여러 신청자를 대기열로 받은 뒤 작성자가 한 명을 선택하게 한다.
4. 채팅 메시지 읽음 여부를 저장한다.
5. 운영자 정산 기록을 별도 테이블로 남긴다.

연습 기능을 구현할 때는 반드시 정상 상황, 권한 없는 상황, 중복 요청 상황 테스트를 함께 작성하자.

---

## 20. 자주 만나는 오류와 확인 순서

### 8080 또는 8081 포트 사용 중

```text
Web server failed to start. Port ... was already in use.
```

같은 포트로 이미 실행 중인 서버가 있다는 뜻이다. 기존 프로세스를 종료하거나 `server.port`를 변경한다.

### WebSocket 클래스 없음

```text
WebSocketMessageBrokerConfigurer.class ... does not exist
```

Gradle 의존성은 추가됐지만 IDE가 아직 다시 읽지 않은 상태일 가능성이 크다. Gradle Reload 후 서버를 완전히 다시 실행한다.

### DB 연결 실패

확인 순서:

1. MySQL 실행 여부
2. `db7` 데이터베이스 존재 여부
3. 사용자명과 비밀번호
4. `application.properties`의 URL
5. 다른 프로그램이 MySQL 포트를 사용 중인지

### 이미지 저장 실패

확인 순서:

1. `C:/upload` 폴더 접근 권한
2. 실제 이미지 파일인지
3. 파일 크기가 10MB 이하인지
4. 요청 전체 크기가 30MB 이하인지

### 버튼은 안 보이는데 URL 직접 호출 가능

화면 조건만 넣고 Service 권한 검사를 빼먹었을 때 생길 수 있다. 운영자 기능은 `@PreAuthorize` 또는 Service 내부 역할 검사가 있는지 확인한다.

### 한글이 깨져 보임

소스 파일, IDE, Gradle 실행 콘솔의 인코딩이 UTF-8인지 확인한다. 파일 내용 자체가 이미 깨져 저장됐다면 IDE 인코딩만 바꿔서는 원래 문장이 돌아오지 않으므로 Git 이전 기록이나 정상 문서를 기준으로 문장을 다시 입력해야 한다.

---

## 21. 포트폴리오에서 강조할 내용

단순히 “게시판을 만들었다”보다 문제와 해결을 함께 말하는 것이 좋다.

### 예시 1: 포인트 안전성

> 도움 미션 등록 후 작성자가 포인트를 먼저 사용해 버리는 문제를 막기 위해 등록 시 보상 포인트를 차감해 미션에 보관했습니다. 양쪽 완료와 운영자 확인 후 수행자에게 지급하고, 정산 여부를 저장해 중복 지급을 막았습니다.

### 예시 2: 권한

> 버튼을 숨기는 화면 권한만 사용하지 않고 Service의 메서드 권한과 작성자 검사를 함께 적용했습니다. 운영자는 글을 삭제하거나 고정할 수 있지만 다른 회원의 글 내용은 수정하지 못하도록 수정 권한과 관리 권한을 나눴습니다.

### 예시 3: 실시간 채팅

> Spring WebSocket과 STOMP로 미션별 채팅 토픽을 구성했습니다. 메시지를 DB에 먼저 저장한 뒤 구독자에게 전송해 새로고침 후에도 기록이 남게 했고, ChannelInterceptor로 확정된 참여자만 전송과 구독을 할 수 있게 제한했습니다.

### 예시 4: 자동 만료

> 수락 요청이 계속 대기 상태로 남지 않도록 스케줄러로 10분 지난 요청을 자동 취소했습니다. 상세 조회 시에도 만료를 확인해 스케줄러 실행 간격 때문에 생기는 시간 차이를 보완했습니다.

### 사용 기술을 말할 때 주의할 점

기술 이름만 나열하지 말고 이 프로젝트에서 어디에 사용했는지 붙여서 말한다.

```text
나쁜 예: JPA, Security, WebSocket을 사용했습니다.

좋은 예: JPA로 회원과 게시글 관계를 모델링했고,
Spring Security로 작성자와 운영자 권한을 나눴으며,
WebSocket과 STOMP로 확정된 미션 참여자 채팅을 구현했습니다.
```

---

## 22. 예상 질문과 답변 연습

### Q. Controller와 Service를 왜 나눴나요?

Controller는 HTTP 요청과 화면 이동을 담당하고 Service는 실제 업무 규칙과 트랜잭션을 담당하도록 책임을 나눴습니다. 그래서 Service를 통합 테스트에서 직접 검증하기 쉽고 같은 규칙이 여러 Controller에 중복되지 않습니다.

### Q. 게시글 수정 권한을 화면에서만 검사하면 안 되나요?

사용자는 URL을 직접 호출할 수 있으므로 화면에서 버튼을 숨기는 것만으로는 부족합니다. Service에서 로그인 아이디와 작성자 아이디를 다시 비교해야 합니다.

### Q. 비밀번호를 어떻게 저장했나요?

BCrypt로 단방향 해시하여 저장했습니다. 변경 시 현재 비밀번호는 `matches()`로 확인하고 새 비밀번호는 다시 `encode()`해서 저장합니다.

### Q. 포인트를 왜 미션 등록 때 차감했나요?

완료 시점에 작성자의 포인트가 부족해지는 상황을 막기 위해서입니다. 등록 시 확보하고, 반려하면 반환하며, 완료 후 수행자에게 지급합니다.

### Q. WebSocket 메시지도 권한 검사가 필요한가요?

필요합니다. 화면을 숨겨도 사용자가 주소를 직접 구독하거나 전송할 수 있기 때문에 STOMP SEND와 SUBSCRIBE 단계에서 현재 로그인 사용자와 미션 참여 관계를 검사했습니다.

### Q. 채팅 메시지를 왜 DB에 저장했나요?

실시간 방송만 하면 접속하지 않았던 사용자는 이전 메시지를 볼 수 없습니다. DB에 저장한 뒤 방송해 실시간성과 기록 보존을 함께 처리했습니다.

### Q. `@Transactional`이 왜 필요한가요?

여러 변경을 하나의 작업으로 묶어 일부만 저장되는 일을 막고, 조회한 엔티티의 변경을 더티 체킹으로 반영하기 위해 사용했습니다.

### Q. 현재 개선하고 싶은 부분은 무엇인가요?

알림 벨의 완전한 실시간 갱신, 미션 취소·환불 정책, 여러 신청자 대기열, 채팅 읽음 처리, 운영자 정산 이력 분리를 추가하면 실제 서비스에 더 가까워질 수 있습니다.

---

## 23. 기능을 수정할 때 체크리스트

새 기능을 추가하거나 기존 기능을 수정할 때 아래 순서로 확인한다.

```text
[ ] 누가 사용할 수 있는 기능인가?
[ ] 로그인만 필요하면 되는가, 작성자 또는 운영자여야 하는가?
[ ] 입력값이 비어 있거나 너무 길면 어떻게 되는가?
[ ] 같은 요청을 두 번 보내면 어떻게 되는가?
[ ] DB의 어느 엔티티 상태가 바뀌는가?
[ ] 중간에 실패하면 이전 변경이 롤백되어야 하는가?
[ ] 화면 새로고침 후에도 데이터가 남아야 하는가?
[ ] 파일을 교체하거나 삭제할 때 실제 파일도 정리되는가?
[ ] 정상 상황 테스트가 있는가?
[ ] 권한 없는 상황 테스트가 있는가?
[ ] 중복 또는 경계값 테스트가 있는가?
```

---

## 24. 주요 파일 빠른 지도

### 실행과 설정

```text
build.gradle
src/main/resources/application.properties
src/main/java/com/example/ex76/Ex76Application.java
src/main/java/com/example/ex76/config/SecurityConfig.java
src/main/java/com/example/ex76/config/WebSocketConfig.java
```

### 계정

```text
controller/AuthController.java
service/ClubMemberServiceImpl.java
security/service/ClubUserDetailsService.java
entity/ClubMember.java
dto/ClubMemberDTO.java
```

### 퀘스트

```text
controller/QuestController.java
service/QuestService.java
entity/DailyQuest.java
entity/Mission.java
repository/DailyQuestRepository.java
templates/quest/dashboard.html
```

### 커뮤니티

```text
controller/CommunityController.java
service/CommunityService.java
service/CommunityImageStorage.java
entity/BoardPost.java
entity/BoardComment.java
repository/BoardPostRepository.java
templates/community/
```

### 마이페이지와 뱃지

```text
controller/MyPageController.java
service/MyPageService.java
service/BadgeService.java
entity/Badge.java
entity/MemberBadge.java
templates/mypage/index.html
```

### 미션 제안·알림·채팅

```text
controller/MissionSuggestionController.java
controller/MissionChatController.java
controller/NotificationController.java
service/MissionSuggestionService.java
service/NotificationService.java
entity/MissionSuggestion.java
entity/MissionSuggestionComment.java
entity/UserNotification.java
templates/suggestion/
templates/notification/
```

### 신고

```text
controller/ReportController.java
service/ReportService.java
entity/UserReport.java
templates/report/
```

---

## 25. 마지막 공부 방법

이 문서를 읽은 뒤에는 반드시 코드를 직접 열어 확인해야 한다. 문서만 읽으면 이해한 것 같지만 실제로 메서드를 찾지 못할 수 있다.

각 기능마다 다음 네 문장을 소리 내어 완성해보자.

```text
이 버튼의 URL은 ______이다.
이 요청을 받는 Controller는 ______이다.
실제 규칙을 처리하는 Service 메서드는 ______이다.
결과적으로 ______ 엔티티의 ______ 값이 바뀐다.
```

그리고 테스트 하나를 골라 다음을 말해본다.

```text
이 테스트가 준비하는 데이터는 무엇인가?
어떤 동작을 실행하는가?
마지막에 어떤 값으로 성공을 판단하는가?
이 테스트가 없다면 어떤 버그를 놓칠 수 있는가?
```

여기까지 자신의 말로 설명할 수 있으면 단순히 코드를 따라 만든 수준을 넘어, 프로젝트의 구조와 이유를 이해하고 있는 상태다.

처음에는 Controller와 Service의 연결만 이해해도 충분하다. 그다음 JPA 관계, Security 권한, 트랜잭션, WebSocket 순서로 넓혀 가자. 한 번에 외우는 것보다 기능 하나를 직접 수정하고 테스트하는 경험이 훨씬 오래 남는다.
