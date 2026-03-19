package com.ssafynity.demo.init;

import com.ssafynity.demo.domain.*;
import com.ssafynity.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 개발용 더미 데이터 초기화 클래스.
 * 삭제하려면 이 파일 전체를 삭제하거나 @Component를 제거하세요.
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final MemberRepository     memberRepository;
    private final PostRepository       postRepository;
    private final CommentRepository    commentRepository;
    private final TechDocRepository    techDocRepository;
    private final TechVideoRepository  techVideoRepository;
    private final EventRepository      eventRepository;
    private final ProjectRepository    projectRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public void run(String... args) {
        // 이미 데이터가 있으면 실행하지 않음 (중복 방지)
        if (memberRepository.count() > 0) return;

        // ──────────── 회원 (4명) ────────────
        Member admin = memberRepository.save(Member.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .nickname("관리자")
                .email("admin@ssafynity.com")
                .bio("SSAFYnity 플랫폼 관리자입니다.")
                .role("ADMIN")
                .build());

        Member kim = memberRepository.save(Member.builder()
                .username("kim_dev")
                .password(passwordEncoder.encode("pass123"))
                .nickname("김개발")
                .email("kim@ssafy.com")
                .bio("백엔드 개발자 지망생. Spring Boot와 JPA를 공부 중입니다.")
                .role("USER")
                .build());

        Member lee = memberRepository.save(Member.builder()
                .username("lee_ssafy")
                .password(passwordEncoder.encode("pass123"))
                .nickname("이싸피")
                .email("lee@ssafy.com")
                .bio("알고리즘 문제 풀이를 즐기는 SSAFY 수료생입니다.")
                .role("USER")
                .build());

        Member park = memberRepository.save(Member.builder()
                .username("park_full")
                .password(passwordEncoder.encode("pass123"))
                .nickname("박풀스택")
                .email("park@ssafy.com")
                .bio("프론트엔드와 백엔드 모두 다루는 풀스택 개발자입니다.")
                .role("USER")
                .build());

        // ──────────── 게시글 (10개) ────────────
        Post p1 = postRepository.save(Post.builder()
                .title("Spring Boot 처음 시작할 때 막히는 것들 정리")
                .content("Spring Boot를 처음 배울 때 많이 헤매는 포인트들을 정리했습니다.\n\n"
                       + "1. 의존성 주입(DI) 개념 이해\n"
                       + "2. application.properties vs application.yml\n"
                       + "3. JPA 엔티티 설계 기초\n"
                       + "4. RESTful API 설계 원칙\n\n"
                       + "댓글로 추가할 내용 알려주시면 업데이트하겠습니다!")
                .author(kim)
                .category("정보")
                .viewCount(142)
                .likeCount(23)
                .build());

        Post p2 = postRepository.save(Post.builder()
                .title("SSAFY 최종 프로젝트 회고 — 6주 동안 배운 것들")
                .content("6주간의 최종 프로젝트가 끝났습니다. 팀원들과 함께 풀스택 서비스를 개발하면서 느낀 점을 공유합니다.\n\n"
                       + "**잘 된 것:**\n- Git Flow 브랜치 전략 도입\n- API 문서화(Swagger)\n- 코드 리뷰 문화\n\n"
                       + "**아쉬운 점:**\n- 테스트 코드 부족\n- 성능 최적화 미흡\n\n"
                       + "다음 프로젝트에서는 꼭 TDD를 도입해보고 싶습니다.")
                .author(park)
                .category("일반")
                .viewCount(89)
                .likeCount(15)
                .build());

        Post p3 = postRepository.save(Post.builder()
                .title("[질문] JPA N+1 문제 해결 방법 질문드립니다")
                .content("안녕하세요. JPA를 사용하다가 N+1 문제가 발생했는데 해결이 잘 안 됩니다.\n\n"
                       + "현재 상황:\n- Post 엔티티에 Author(Member)가 @ManyToOne LAZY 로딩\n"
                       + "- 목록 조회 시 각 Post마다 author를 조회하는 쿼리가 추가 발생\n\n"
                       + "fetch join을 써봤는데 페이징이 안 됩니다. 어떻게 해결해야 할까요?")
                .author(lee)
                .category("질문")
                .viewCount(67)
                .likeCount(8)
                .build());

        Post p4 = postRepository.save(Post.builder()
                .title("알고리즘 스터디 멤버 모집합니다 (주 2회)")
                .content("매주 화, 목 저녁 8시에 온라인으로 진행하는 알고리즘 스터디입니다.\n\n"
                       + "- 플랫폼: 백준, 프로그래머스\n"
                       + "- 방식: 각자 풀고 코드 리뷰\n"
                       + "- 수준: 브론즈~실버 위주 (점차 난이도 상향)\n\n"
                       + "참여 원하시는 분은 댓글 달아주세요!")
                .author(kim)
                .category("일반")
                .viewCount(201)
                .likeCount(31)
                .build());

        Post p5 = postRepository.save(Post.builder()
                .title("React Query vs Zustand — 언제 무엇을 써야 할까?")
                .content("프론트엔드 상태 관리에 대해 정리해봤습니다.\n\n"
                       + "**React Query (TanStack Query)**\n서버 상태 동기화에 특화. 캐싱, 재요청, 로딩 상태 관리가 편합니다.\n\n"
                       + "**Zustand**\n클라이언트 전역 상태 관리에 최적. 보일러플레이트가 적고 가벼워요.\n\n"
                       + "결론: 서버 데이터는 React Query, UI 전역 상태는 Zustand로 역할을 분리하는 것이 좋습니다.")
                .author(park)
                .category("정보")
                .viewCount(178)
                .likeCount(42)
                .build());

        Post p6 = postRepository.save(Post.builder()
                .title("[공지] SSAFYnity 플랫폼 베타 오픈!")
                .content("안녕하세요, SSAFYnity 관리자입니다.\n\n"
                       + "SSAFY 구성원들을 위한 커뮤니티 플랫폼 **SSAFYnity**가 베타 오픈했습니다!\n\n"
                       + "주요 기능:\n- 커뮤니티 게시판\n- 기술 문서 공유\n- 강의 영상 아카이브\n"
                       + "- 이벤트/스터디 모집\n- 프로젝트 쇼케이스\n\n"
                       + "피드백과 제안은 언제든지 환영합니다!")
                .author(admin)
                .category("공지")
                .viewCount(512)
                .likeCount(87)
                .build());

        Post p7 = postRepository.save(Post.builder()
                .title("개발자 커리어 전환 후기 — 비전공자 SSAFY 합격기")
                .content("비전공자로 SSAFY에 합격하기까지의 준비 과정을 공유합니다.\n\n"
                       + "1. 코딩 테스트 준비 (6개월): 파이썬으로 알고리즘 기초부터\n"
                       + "2. 웹 개발 기초: HTML/CSS/JS, React 기초\n"
                       + "3. SSAFY 지원서 및 에세이 작성 팁\n"
                       + "4. 면접 준비: CS 기초 질문 정리\n\n"
                       + "포기하지 않으면 분명 길이 있습니다. 화이팅!")
                .author(lee)
                .category("일반")
                .viewCount(334)
                .likeCount(58)
                .build());

        Post p8 = postRepository.save(Post.builder()
                .title("Docker + Spring Boot 배포 삽질기")
                .content("처음으로 Docker로 Spring Boot 앱을 배포해봤는데 정말 많이 헤맸습니다.\n\n"
                       + "문제들:\n- Dockerfile COPY 경로 문제\n- DB 연결 (host.docker.internal)\n"
                       + "- 환경변수 주입 방법 (docker-compose env_file)\n\n"
                       + "결국 해결했고 지식을 공유합니다. Dockerfile 예시도 첨부했어요.")
                .author(kim)
                .category("정보")
                .viewCount(156)
                .likeCount(29)
                .build());

        Post p9 = postRepository.save(Post.builder()
                .title("[잡담] SSAFY 끝나고 취업 준비 어떻게 하시나요?")
                .content("SSAFY 수료 후 취업 준비 중인데요, 여러분은 어떻게 준비하고 계신가요?\n\n"
                       + "저는 지금 알고리즘 + 포트폴리오 정리에 집중하고 있는데 뭔가 방향이 맞는 건지 모르겠어서요.\n\n"
                       + "취업하신 분들 조언 부탁드립니다!")
                .author(park)
                .category("잡담")
                .viewCount(445)
                .likeCount(63)
                .build());

        Post p10 = postRepository.save(Post.builder()
                .title("Git 협업 워크플로우 — 팀 프로젝트에서 쓰는 실전 전략")
                .content("팀 프로젝트에서 Git을 잘 쓰는 법을 정리했습니다.\n\n"
                       + "**브랜치 전략 (Git Flow)**\n- main: 배포 브랜치\n"
                       + "- develop: 개발 통합 브랜치\n- feature/이름: 기능 개발\n\n"
                       + "**커밋 메시지 컨벤션**\n`feat:`, `fix:`, `docs:`, `refactor:`, `test:`\n\n"
                       + "**PR 리뷰 규칙**\n- 최소 1명 Approve 필요\n- 셀프 머지 금지")
                .author(lee)
                .category("정보")
                .viewCount(267)
                .likeCount(48)
                .build());

        // ──────────── 댓글 (5개) ────────────
        commentRepository.save(Comment.builder()
                .content("정말 유익한 정보 감사합니다! 저도 N+1 문제로 고생했는데 이 글 보고 해결했어요.")
                .author(park)
                .post(p3)
                .build());

        commentRepository.save(Comment.builder()
                .content("Fetch Join + @BatchSize 조합을 쓰면 페이징도 되고 N+1도 해결됩니다. 공식 문서 참고해보세요!")
                .author(kim)
                .post(p3)
                .build());

        commentRepository.save(Comment.builder()
                .content("알고리즘 스터디 참여하고 싶습니다! 연락은 어떻게 드리나요?")
                .author(lee)
                .post(p4)
                .build());

        commentRepository.save(Comment.builder()
                .content("React Query 정말 편하죠. 저도 서버 상태는 React Query, 클라이언트 상태는 Zustand로 나눠서 씁니다!")
                .author(kim)
                .post(p5)
                .build());

        commentRepository.save(Comment.builder()
                .content("docker-compose로 하면 더 편합니다. docker-compose.yml 예시도 공유해주시면 좋겠어요!")
                .author(lee)
                .post(p8)
                .build());

        // ──────────── 기술 문서 (5개) ────────────
        techDocRepository.save(TechDoc.builder()
                .title("Spring Boot 입문 — 나만의 첫 REST API 만들기")
                .content("# Spring Boot REST API 입문\n\n"
                       + "## 프로젝트 생성\nspring.io/start 에서 Web, JPA, H2 의존성을 선택합니다.\n\n"
                       + "## 엔티티 정의\n```java\n@Entity\npublic class Item {\n  @Id @GeneratedValue\n  private Long id;\n  private String name;\n}\n```\n\n"
                       + "## Repository\n```java\npublic interface ItemRepository extends JpaRepository<Item, Long> {}\n```\n\n"
                       + "## Controller\n```java\n@RestController\n@RequestMapping(\"/api/items\")\npublic class ItemController {\n  @GetMapping public List<Item> list() { ... }\n}\n```\n\n"
                       + "이것만으로 CRUD API가 완성됩니다!")
                .author(admin)
                .category("튜토리얼")
                .tags("Spring Boot,REST API,JPA,입문")
                .viewCount(380)
                .pinned(true)
                .build());

        techDocRepository.save(TechDoc.builder()
                .title("JPA 연관관계 완전 정복 — @OneToMany, @ManyToOne 실전 가이드")
                .content("# JPA 연관관계 가이드\n\n"
                       + "## 단방향 vs 양방향\n연관관계는 가능하면 단방향으로 설계합니다.\n\n"
                       + "## @ManyToOne (다 → 1)\n```java\n@ManyToOne(fetch = FetchType.LAZY)\n@JoinColumn(name = \"member_id\")\nprivate Member author;\n```\n\n"
                       + "## Fetch 전략\n- **LAZY**: 실제 사용 시점에 SELECT (권장)\n- **EAGER**: 즉시 JOIN SELECT (N+1 위험)\n\n"
                       + "## N+1 해결법\n1. `@EntityGraph`\n2. `fetch join` (JPQL)\n3. `@BatchSize`")
                .author(kim)
                .category("데이터베이스")
                .tags("JPA,연관관계,N+1,Hibernate")
                .viewCount(512)
                .pinned(true)
                .build());

        techDocRepository.save(TechDoc.builder()
                .title("알고리즘 문제 유형별 접근법 — BFS/DFS/DP 패턴")
                .content("# 알고리즘 유형별 패턴\n\n"
                       + "## BFS (너비 우선 탐색)\n최단 경로, 레벨 탐색에 사용합니다.\n```java\nQueue<int[]> q = new LinkedList<>();\nq.offer(new int[]{시작x, 시작y});\n```\n\n"
                       + "## DFS (깊이 우선 탐색)\n모든 경우 탐색, 순열/조합에 사용합니다.\n```java\nvoid dfs(int depth) {\n  if (depth == N) { 처리; return; }\n  for (int i ...) { dfs(depth+1); }\n}\n```\n\n"
                       + "## DP (동적 프로그래밍)\n점화식을 먼저 세우고 메모이제이션을 적용합니다.")
                .author(lee)
                .category("알고리즘")
                .tags("BFS,DFS,DP,알고리즘")
                .viewCount(445)
                .pinned(false)
                .build());

        techDocRepository.save(TechDoc.builder()
                .title("React 18 핵심 정리 — Concurrent Features & Hooks")
                .content("# React 18 핵심 기능\n\n"
                       + "## Concurrent Rendering\n`startTransition`으로 긴급하지 않은 업데이트를 표시합니다.\n```jsx\nstartTransition(() => setState(newState));\n```\n\n"
                       + "## useTransition\n```jsx\nconst [isPending, startTransition] = useTransition();\n```\n\n"
                       + "## useDeferredValue\n값의 업데이트를 지연시켜 UI 반응성을 유지합니다.\n\n"
                       + "## Suspense 개선\n비동기 데이터 로딩에 Suspense를 활용합니다.")
                .author(park)
                .category("프론트엔드")
                .tags("React,React18,Hooks,Concurrent")
                .viewCount(298)
                .pinned(false)
                .build());

        techDocRepository.save(TechDoc.builder()
                .title("Docker & Docker Compose 실전 가이드")
                .content("# Docker 실전 가이드\n\n"
                       + "## Dockerfile 기본\n```dockerfile\nFROM openjdk:21-slim\nCOPY build/libs/app.jar app.jar\nENTRYPOINT [\"java\", \"-jar\", \"app.jar\"]\n```\n\n"
                       + "## docker-compose.yml\n```yaml\nservices:\n  app:\n    build: .\n    ports:\n      - \"8080:8080\"\n  db:\n    image: mysql:8\n    environment:\n      MYSQL_ROOT_PASSWORD: root\n```\n\n"
                       + "## 자주 쓰는 명령어\n- `docker-compose up -d`\n- `docker-compose logs -f`\n- `docker exec -it <id> bash`")
                .author(kim)
                .category("DevOps")
                .tags("Docker,Docker Compose,DevOps,배포")
                .viewCount(334)
                .pinned(false)
                .build());

        // ──────────── 기술 영상 (4개) ────────────
        techVideoRepository.save(TechVideo.builder()
                .title("Spring Boot 3.x + JPA 실전 강의 (전체)")
                .description("Spring Boot 3.x와 Spring Data JPA를 활용한 실전 프로젝트 개발 강의입니다. "
                            + "엔티티 설계부터 REST API 완성까지 전 과정을 다룹니다.")
                .youtubeId("9SGDpanrc8U")
                .duration("2:14:30")
                .author(admin)
                .category("강의")
                .tags("Spring Boot,JPA,REST API")
                .viewCount(1240)
                .pinned(true)
                .build());

        techVideoRepository.save(TechVideo.builder()
                .title("백준 알고리즘 BFS/DFS 유형 완전 정복")
                .description("BFS와 DFS를 활용한 백준 알고리즘 문제 풀이 시리즈입니다. "
                            + "기초 개념부터 응용 문제까지 단계별로 설명합니다.")
                .youtubeId("7C9RgOcvkvo")
                .duration("45:20")
                .author(lee)
                .category("강의")
                .tags("알고리즘,BFS,DFS,백준")
                .viewCount(876)
                .pinned(true)
                .build());

        techVideoRepository.save(TechVideo.builder()
                .title("SSAFY 최종 프로젝트 발표 — SSAFYnity 개발기")
                .description("SSAFY 최종 프로젝트로 개발한 커뮤니티 플랫폼 SSAFYnity의 "
                            + "개발 과정과 기술 스택을 소개합니다.")
                .youtubeId("dQw4w9WgXcQ")
                .duration("18:45")
                .author(park)
                .category("프로젝트발표")
                .tags("Spring Boot,React,프로젝트")
                .viewCount(432)
                .pinned(false)
                .build());

        techVideoRepository.save(TechVideo.builder()
                .title("실무 코드 리뷰 — Spring Boot 코드 품질 개선")
                .description("실제 Spring Boot 코드를 리뷰하면서 코드 품질을 개선하는 과정을 보여줍니다. "
                            + "SOLID 원칙, 테스트 코드 작성, 리팩토링 등을 다룹니다.")
                .youtubeId("jNQXAC9IVRw")
                .duration("38:10")
                .author(kim)
                .category("코드리뷰")
                .tags("Spring Boot,코드리뷰,리팩토링,SOLID")
                .viewCount(567)
                .pinned(false)
                .build());

        // ──────────── 이벤트 (3개) ────────────
        eventRepository.save(Event.builder()
                .title("Spring Boot & JPA 집중 스터디 (4기)")
                .description("Spring Boot와 JPA를 함께 공부하는 온라인 스터디입니다.\n"
                           + "매주 토요일 오전 10시, 4주간 진행합니다.\n\n"
                           + "**커리큘럼:**\n1주차: Spring Boot 기초 + REST API\n"
                           + "2주차: JPA 엔티티 설계\n3주차: QueryDSL 활용\n4주차: 실전 프로젝트 완성")
                .startDate(LocalDateTime.of(2026, 4, 12, 10, 0))
                .endDate(LocalDateTime.of(2026, 5, 3, 12, 0))
                .location("ONLINE")
                .eventType("스터디")
                .organizer(kim)
                .maxParticipants(20)
                .currentParticipants(12)
                .status("UPCOMING")
                .build());

        eventRepository.save(Event.builder()
                .title("SSAFY 해커톤 2026 — AI 서비스 개발 챌린지")
                .description("SSAFY 구성원이 팀을 구성해 48시간 동안 AI 서비스를 개발하는 해커톤입니다.\n\n"
                           + "**주제:** AI를 활용한 일상 문제 해결\n**팀 구성:** 3~5인\n"
                           + "**시상:** 대상 100만원, 최우수상 50만원, 우수상 30만원\n\n"
                           + "팀 빌딩은 이벤트 페이지에서 신청해주세요.")
                .startDate(LocalDateTime.of(2026, 5, 1, 18, 0))
                .endDate(LocalDateTime.of(2026, 5, 3, 18, 0))
                .location("OFFLINE")
                .eventType("해커톤")
                .organizer(admin)
                .maxParticipants(100)
                .currentParticipants(47)
                .status("UPCOMING")
                .build());

        eventRepository.save(Event.builder()
                .title("월간 코드 리뷰 세션 — 3월")
                .description("매달 진행하는 코드 리뷰 세션입니다. 참가자가 자신의 코드를 공유하고 "
                           + "피드백을 주고받습니다.\n\n"
                           + "**이번 달 주제:** Spring Boot API 서버 코드\n"
                           + "**방식:** Zoom 화상회의, 코드 공유 후 15분씩 리뷰")
                .startDate(LocalDateTime.of(2026, 3, 28, 20, 0))
                .endDate(LocalDateTime.of(2026, 3, 28, 22, 0))
                .location("ONLINE")
                .eventType("세미나")
                .organizer(park)
                .maxParticipants(15)
                .currentParticipants(9)
                .status("ONGOING")
                .build());

        // ──────────── 프로젝트 (4개) ────────────
        projectRepository.save(Project.builder()
                .title("SSAFYnity — SSAFY 커뮤니티 플랫폼")
                .description("SSAFY 구성원들을 위한 통합 커뮤니티 플랫폼입니다.\n\n"
                           + "**주요 기능:**\n- 커뮤니티 게시판 (질문, 정보, 잡담)\n"
                           + "- 기술 문서 공유 및 북마크\n- 교육 영상 아카이브\n"
                           + "- 이벤트/스터디 모집\n- 프로젝트 쇼케이스\n\n"
                           + "**개발 기간:** 6주 (SSAFY 최종 프로젝트)")
                .techStack("Spring Boot,Spring Data JPA,QueryDSL,Thymeleaf,H2,Gradle")
                .githubUrl("https://github.com/ssafynity/ssafynity")
                .demoUrl("http://localhost:8080")
                .author(park)
                .teamSize(4)
                .status("IN_PROGRESS")
                .likeCount(42)
                .viewCount(312)
                .build());

        projectRepository.save(Project.builder()
                .title("AlgoTracker — 알고리즘 학습 진도 관리 앱")
                .description("백준, 프로그래머스 문제 풀이 현황을 시각화하는 학습 진도 관리 앱입니다.\n\n"
                           + "**핵심 기능:**\n- 문제 태그별 통계 차트\n"
                           + "- 스트릭(연속 풀이) 현황 표시\n"
                           + "- 틀린 문제 오답노트 기능\n"
                           + "- 친구와 진도 비교 기능")
                .techStack("React,TypeScript,Node.js,Express,PostgreSQL")
                .githubUrl("https://github.com/lee_ssafy/algotracker")
                .author(lee)
                .teamSize(2)
                .status("COMPLETED")
                .likeCount(28)
                .viewCount(187)
                .build());

        projectRepository.save(Project.builder()
                .title("DevDiary — 개발자 일일 회고 서비스")
                .description("매일 개발 학습을 기록하고 공유하는 회고 서비스입니다.\n\n"
                           + "GitHub Actions로 매일 밤 12시에 회고 작성 알림을 보냅니다.\n\n"
                           + "**특징:**\n- 마크다운 에디터 지원\n- 태그 기반 분류\n"
                           + "- 월별 회고 달력 뷰\n- 공개/비공개 선택")
                .techStack("Spring Boot,React,MySQL,Docker,GitHub Actions")
                .githubUrl("https://github.com/kim_dev/devdiary")
                .demoUrl("https://devdiary.example.com")
                .author(kim)
                .teamSize(1)
                .status("COMPLETED")
                .likeCount(35)
                .viewCount(245)
                .build());

        projectRepository.save(Project.builder()
                .title("InterviewPrepKit — 기술 면접 준비 도구")
                .description("개발자 기술 면접을 체계적으로 준비할 수 있는 학습 도구입니다.\n\n"
                           + "**포함 내용:**\n- CS 기초 (자료구조, OS, 네트워크, DB)\n"
                           + "- Spring/Java 면접 질문 200+\n"
                           + "- 모의 면접 타이머\n"
                           + "- 답변 저장 및 복습 기능\n\n"
                           + "SSAFY 수료 후 취업 준비 중에 직접 만든 도구입니다.")
                .techStack("Vue.js,Spring Boot,H2,Bootstrap")
                .githubUrl("https://github.com/park_full/interview-prep-kit")
                .author(park)
                .teamSize(3)
                .status("COMPLETED")
                .likeCount(61)
                .viewCount(408)
                .build());
    }
}
