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
    private final NotificationRepository  notificationRepository;
    private final BookmarkRepository      bookmarkRepository;
    private final MentorProfileRepository mentorProfileRepository;
    private final MentoringRequestRepository mentoringRequestRepository;
    private final FriendshipRepository friendshipRepository;
    private final ChatRoomRepository   chatRoomRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public void run(String... args) {
        // 테스트 계정은 항상 (없는 경우만) 생성
        initTestAccounts();
        // 채팅방은 항상 (없는 경우만) 생성
        initChatRooms();

        // 이미 데이터가 있으면 나머지 실행하지 않음 (중복 방지)
        if (memberRepository.count() > 200) return;

        // ──────────── 회원 (6명) ────────────
        Member admin = memberRepository.save(Member.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .nickname("관리자")
                .email("admin@ssafynity.com")
                .bio("SSAFYnity 플랫폼 관리자입니다. 플랫폼 운영과 콘텐츠 관리를 담당합니다.")
                .campus("서울")
                .cohort(13)
                .classCode(1)
                .realName("관리자")
                .role("ADMIN")
                .build());

        Member kim = memberRepository.save(Member.builder()
                .username("kim_dev")
                .password(passwordEncoder.encode("pass123"))
                .nickname("김개발")
                .email("kim@ssafy.com")
                .bio("백엔드 개발자 지망생. Spring Boot와 JPA를 공부 중입니다. 매일 한 문제씩 알고리즘 풀기 중.")
                .campus("서울")
                .cohort(13)
                .classCode(1)
                .realName("김민준")
                .profileImageUrl("https://picsum.photos/seed/kim_dev/200/200")
                .role("USER")
                .build());

        Member lee = memberRepository.save(Member.builder()
                .username("lee_ssafy")
                .password(passwordEncoder.encode("pass123"))
                .nickname("이싸피")
                .email("lee@ssafy.com")
                .bio("알고리즘 문제 풀이를 즐기는 SSAFY 수료생. 현재 백엔드 취준 중입니다.")
                .campus("대전")
                .cohort(13)
                .classCode(2)
                .realName("이지훈")
                .profileImageUrl("https://picsum.photos/seed/lee_ssafy/200/200")
                .role("USER")
                .build());

        Member park = memberRepository.save(Member.builder()
                .username("park_full")
                .password(passwordEncoder.encode("pass123"))
                .nickname("박풀스택")
                .email("park@ssafy.com")
                .bio("프론트엔드와 백엔드 모두 다루는 풀스택 개발자. React + Spring Boot 조합을 좋아합니다.")
                .campus("서울")
                .cohort(13)
                .classCode(1)
                .realName("박준혁")
                .profileImageUrl("https://picsum.photos/seed/park_full/200/200")
                .role("USER")
                .build());

        Member choi = memberRepository.save(Member.builder()
                .username("choi_cloud")
                .password(passwordEncoder.encode("pass123"))
                .nickname("최클라우드")
                .email("choi@ssafy.com")
                .bio("DevOps 엔지니어 지망생. AWS, Docker, Kubernetes를 공부하고 있습니다.")
                .campus("구미")
                .cohort(12)
                .classCode(1)
                .realName("최성환")
                .profileImageUrl("https://picsum.photos/seed/choi_cloud/200/200")
                .role("USER")
                .build());

        Member jung = memberRepository.save(Member.builder()
                .username("jung_ai")
                .password(passwordEncoder.encode("pass123"))
                .nickname("정AI")
                .email("jung@ssafy.com")
                .bio("AI/ML 개발자. Python과 PyTorch를 주로 사용하며 MLOps에 관심이 많습니다.")
                .campus("부울경")
                .cohort(14)
                .classCode(2)
                .realName("정유진")
                .profileImageUrl("https://picsum.photos/seed/jung_ai/200/200")
                .role("USER")
                .build());

        // ──────────── 게시글 (12개, HTML 포맷) ────────────
        Post p1 = postRepository.save(Post.builder()
                .title("Spring Boot 처음 시작할 때 막히는 것들 정리")
                .content("<h2>Spring Boot 입문자가 자주 막히는 포인트</h2>"
                       + "<p>Spring Boot를 처음 배울 때 많이 헤매는 부분들을 정리했습니다. 댓글로 추가할 내용 알려주시면 업데이트하겠습니다!</p>"
                       + "<img src=\"https://picsum.photos/seed/spring/800/400\" style=\"max-width:100%;border-radius:8px;margin:12px 0\">"
                       + "<h3>1. 의존성 주입(DI) 이해하기</h3>"
                       + "<p><strong>@Autowired</strong> 보다 <strong>생성자 주입</strong>을 권장합니다. Lombok의 <code>@RequiredArgsConstructor</code>와 함께 사용하면 편리합니다.</p>"
                       + "<h3>2. application.properties vs application.yml</h3>"
                       + "<p>두 형식은 동일한 설정을 다르게 표현합니다. 팀 내 통일이 중요하며, yml이 계층 구조가 명확해 가독성이 높습니다.</p>"
                       + "<h3>3. JPA 엔티티 설계 기초</h3>"
                       + "<p><code>@Entity</code>, <code>@Id</code>, <code>@GeneratedValue</code>는 반드시 알아야 합니다. 연관관계는 처음엔 단방향으로만 설계하세요.</p>"
                       + "<h3>4. RESTful API 설계 원칙</h3>"
                       + "<ul><li>GET: 조회</li><li>POST: 생성</li><li>PUT/PATCH: 수정</li><li>DELETE: 삭제</li></ul>")
                .author(kim)
                .category("정보")
                .viewCount(284)
                .likeCount(41)
                .build());

        Post p2 = postRepository.save(Post.builder()
                .title("SSAFY 최종 프로젝트 회고 — 6주 동안 배운 것들")
                .content("<h2>6주간의 최종 프로젝트 회고</h2>"
                       + "<p>팀원 4명과 함께 풀스택 서비스를 개발하면서 느낀 점을 솔직하게 공유합니다.</p>"
                       + "<img src=\"https://picsum.photos/seed/teamwork/800/350\" style=\"max-width:100%;border-radius:8px;margin:12px 0\">"
                       + "<h3>✅ 잘 된 것들</h3>"
                       + "<ul><li><strong>Git Flow 브랜치 전략 도입</strong> — 충돌이 거의 없었습니다</li>"
                       + "<li><strong>API 문서화(Swagger)</strong> — 프론트/백 협업이 매끄러웠습니다</li>"
                       + "<li><strong>데일리 스크럼</strong> — 매일 10분 회의로 블로킹 이슈를 빠르게 해결했습니다</li></ul>"
                       + "<h3>❌ 아쉬운 점들</h3>"
                       + "<ul><li>테스트 코드가 거의 없었음 — 배포 시 불안했습니다</li>"
                       + "<li>성능 최적화를 마지막에야 생각했음 — N+1 문제가 뒤늦게 발견됨</li></ul>"
                       + "<p>다음 프로젝트에서는 꼭 TDD를 도입해보고 싶습니다. 이번 경험이 큰 자산이 됐습니다! 🙌</p>")
                .author(park)
                .category("일반")
                .viewCount(167)
                .likeCount(28)
                .build());

        Post p3 = postRepository.save(Post.builder()
                .title("[질문] JPA N+1 문제 해결 방법 아시는 분 계신가요?")
                .content("<h2>JPA N+1 문제로 고생 중입니다 😭</h2>"
                       + "<p>안녕하세요. JPA를 사용하다가 N+1 문제가 발생했는데 해결이 잘 안 됩니다.</p>"
                       + "<h3>현재 상황</h3>"
                       + "<ul>"
                       + "<li>Post 엔티티에 Author(Member)가 <code>@ManyToOne(fetch = LAZY)</code></li>"
                       + "<li>목록 조회 시 각 Post마다 author를 조회하는 쿼리가 추가 발생</li>"
                       + "<li>게시글 10개면 쿼리가 11번 실행됨</li>"
                       + "</ul>"
                       + "<h3>시도해본 것들</h3>"
                       + "<p>fetch join을 써봤는데 페이징(<code>Pageable</code>)과 함께 쓰면 <strong>HHH90003004 경고</strong>가 뜨면서 메모리에서 전체 조회 후 페이징을 합니다.</p>"
                       + "<p>어떻게 해결해야 할까요? <code>@BatchSize</code>도 써봤는데 IN 쿼리로 묶여서 나오긴 하는데 이게 맞는 방법인가요?</p>")
                .author(lee)
                .category("질문")
                .viewCount(134)
                .likeCount(12)
                .build());

        Post p4 = postRepository.save(Post.builder()
                .title("알고리즘 스터디 멤버 모집합니다! (주 2회, 온라인)")
                .content("<h2>🔥 알고리즘 스터디 4기 모집</h2>"
                       + "<p>매주 <strong>화, 목 저녁 9시</strong>에 Discord에서 진행하는 알고리즘 스터디입니다.</p>"
                       + "<img src=\"https://picsum.photos/seed/algo/800/300\" style=\"max-width:100%;border-radius:8px;margin:12px 0\">"
                       + "<h3>📋 스터디 정보</h3>"
                       + "<ul>"
                       + "<li><strong>플랫폼:</strong> 백준, 프로그래머스</li>"
                       + "<li><strong>방식:</strong> 각자 풀고 코드 리뷰 + 풀이 발표</li>"
                       + "<li><strong>수준:</strong> 실버 ~ 골드 위주 (점차 난이도 상향)</li>"
                       + "<li><strong>인원:</strong> 5~8명</li>"
                       + "</ul>"
                       + "<h3>✅ 지원 조건</h3>"
                       + "<p>백준 실버 이상 또는 프로그래머스 Lv.2 이상 풀어본 경험이 있으신 분. 주 2회 참여 가능하신 분.</p>"
                       + "<p>참여 원하시는 분은 댓글에 <strong>백준 아이디</strong>와 <strong>현재 티어</strong> 남겨주세요! 🙌</p>")
                .author(lee)
                .category("일반")
                .viewCount(389)
                .likeCount(54)
                .build());

        Post p5 = postRepository.save(Post.builder()
                .title("React Query vs Zustand — 언제 무엇을 써야 할까?")
                .content("<h2>프론트엔드 상태 관리 완벽 정리</h2>"
                       + "<p>React 앱에서 상태 관리를 어떻게 할지 항상 고민되시죠? 두 라이브러리의 역할을 명확히 구분해봤습니다.</p>"
                       + "<h3>🔵 React Query (TanStack Query)</h3>"
                       + "<p><strong>서버 상태 동기화</strong>에 특화된 라이브러리입니다.</p>"
                       + "<ul>"
                       + "<li>자동 캐싱 & 백그라운드 리페치</li>"
                       + "<li>로딩/에러 상태 자동 관리</li>"
                       + "<li>Stale-While-Revalidate 전략 지원</li>"
                       + "</ul>"
                       + "<h3>🟢 Zustand</h3>"
                       + "<p><strong>클라이언트 전역 상태 관리</strong>에 최적화된 경량 라이브러리입니다.</p>"
                       + "<ul>"
                       + "<li>보일러플레이트 최소화</li>"
                       + "<li>Redux보다 훨씬 간단한 API</li>"
                       + "<li>미들웨어 지원 (immer, persist 등)</li>"
                       + "</ul>"
                       + "<h3>💡 결론</h3>"
                       + "<p><strong>서버에서 오는 데이터</strong>는 React Query, <strong>UI 전역 상태</strong>(다크모드, 모달 상태 등)는 Zustand로 역할을 분리하는 것이 최선입니다.</p>")
                .author(park)
                .category("정보")
                .viewCount(312)
                .likeCount(67)
                .build());

        Post p6 = postRepository.save(Post.builder()
                .title("[공지] SSAFYnity 플랫폼 정식 오픈!")
                .content("<h2>🎉 SSAFYnity 정식 오픈을 알립니다!</h2>"
                       + "<p>안녕하세요, SSAFYnity 관리자입니다. SSAFY 구성원들을 위한 통합 커뮤니티 플랫폼 <strong>SSAFYnity</strong>가 정식 오픈했습니다!</p>"
                       + "<img src=\"https://picsum.photos/seed/launch/800/400\" style=\"max-width:100%;border-radius:8px;margin:12px 0\">"
                       + "<h3>🚀 주요 기능</h3>"
                       + "<ul>"
                       + "<li>📝 <strong>커뮤니티</strong> — 질문, 정보, 잡담 게시판</li>"
                       + "<li>📚 <strong>기술 문서</strong> — 기술 노하우 공유 및 북마크</li>"
                       + "<li>🎬 <strong>영상 아카이브</strong> — 강의·발표 영상 모음</li>"
                       + "<li>📅 <strong>이벤트</strong> — 스터디·해커톤 모집</li>"
                       + "<li>🏆 <strong>프로젝트</strong> — SSAFY 프로젝트 쇼케이스</li>"
                       + "<li>💬 <strong>실시간 채팅</strong> — 채널별 단체 채팅</li>"
                       + "</ul>"
                       + "<p>여러분의 소중한 피드백과 제안을 언제든지 환영합니다. 함께 성장하는 커뮤니티를 만들어가요! 💪</p>")
                .author(admin)
                .category("공지")
                .viewCount(891)
                .likeCount(142)
                .build());

        Post p7 = postRepository.save(Post.builder()
                .title("비전공자 SSAFY 합격 후기 — 6개월 준비 과정 공개")
                .content("<h2>비전공자도 할 수 있다! SSAFY 합격 후기</h2>"
                       + "<p>문과 출신으로 개발자 커리어를 시작하기까지의 6개월 준비 과정을 솔직하게 공개합니다.</p>"
                       + "<img src=\"https://picsum.photos/seed/career/800/350\" style=\"max-width:100%;border-radius:8px;margin:12px 0\">"
                       + "<h3>📅 월별 준비 과정</h3>"
                       + "<ul>"
                       + "<li><strong>1~2월:</strong> Python 기초 문법 + 백준 Bronze 완파</li>"
                       + "<li><strong>3~4월:</strong> HTML/CSS/JS 기초, 간단한 웹페이지 제작</li>"
                       + "<li><strong>5월:</strong> 자소서 작성 + 코딩 테스트 집중 준비</li>"
                       + "<li><strong>6월:</strong> 최종 면접 준비 (CS 기초, 프로젝트 발표 연습)</li>"
                       + "</ul>"
                       + "<h3>💡 합격 팁</h3>"
                       + "<p>코딩 테스트보다 에세이와 인터뷰가 훨씬 중요했습니다. <strong>\"왜 개발자가 되고 싶은가\"</strong>를 명확하게 답할 수 있어야 합니다.</p>"
                       + "<p>포기하지 않으면 분명 길이 있습니다. 모두 화이팅! 🔥</p>")
                .author(jung)
                .category("일반")
                .viewCount(567)
                .likeCount(98)
                .build());

        Post p8 = postRepository.save(Post.builder()
                .title("Docker + Spring Boot 배포 완전 정복 — 삽질 기록")
                .content("<h2>Docker로 Spring Boot 배포하면서 겪은 것들</h2>"
                       + "<p>처음으로 Docker로 Spring Boot 앱을 EC2에 배포해봤는데 정말 많이 헤맸습니다. 삽질 기록을 공유합니다.</p>"
                       + "<h3>🐋 Dockerfile 최종본</h3>"
                       + "<pre><code>FROM openjdk:21-slim\nARG JAR_FILE=build/libs/*.jar\nCOPY ${JAR_FILE} app.jar\nENTRYPOINT [\"java\",\"-jar\",\"/app.jar\"]</code></pre>"
                       + "<h3>🔥 겪었던 문제들</h3>"
                       + "<ul>"
                       + "<li><strong>COPY 경로 문제:</strong> Gradle 빌드 결과물 경로가 매번 달라서 와일드카드 <code>*.jar</code> 사용으로 해결</li>"
                       + "<li><strong>DB 연결:</strong> 컨테이너에서 호스트 DB에 접근할 때 <code>host.docker.internal</code> 사용</li>"
                       + "<li><strong>환경변수:</strong> docker-compose의 <code>env_file</code>로 민감한 정보 분리</li>"
                       + "</ul>"
                       + "<p>한번 설정해두면 배포가 너무 편해집니다. 꼭 익혀두세요!</p>")
                .author(choi)
                .category("정보")
                .viewCount(445)
                .likeCount(76)
                .build());

        Post p9 = postRepository.save(Post.builder()
                .title("[잡담] SSAFY 끝나고 취업 준비 어떻게 하고 계신가요?")
                .content("<h2>취준생 집합! 😅</h2>"
                       + "<p>SSAFY 수료한 지 두 달째인데... 솔직히 막막합니다. 다들 어떻게 준비하고 계신지 궁금해서 글 올려봐요.</p>"
                       + "<h3>현재 제 상황</h3>"
                       + "<ul>"
                       + "<li>코딩 테스트: 매일 1~2문제 풀이 중 (프로그래머스 Lv.2~3)</li>"
                       + "<li>포트폴리오: SSAFY 프로젝트 리팩토링 중</li>"
                       + "<li>CS 공부: 운영체제, 네트워크 위주로 정리 중</li>"
                       + "</ul>"
                       + "<p>대기업은 포기하고 중견/중소 위주로 노리고 있는데 이 방향이 맞는 건지... 취업에 성공하신 분들 조언 부탁드립니다! 🙏</p>")
                .author(park)
                .category("잡담")
                .viewCount(728)
                .likeCount(89)
                .build());

        Post p10 = postRepository.save(Post.builder()
                .title("Git 협업 워크플로우 — 팀 프로젝트 실전 전략")
                .content("<h2>팀 프로젝트에서 Git 잘 쓰는 법</h2>"
                       + "<p>SSAFY 프로젝트를 하면서 정착한 Git 협업 방식을 공유합니다.</p>"
                       + "<h3>🌿 브랜치 전략 (Git Flow)</h3>"
                       + "<ul>"
                       + "<li><code>main</code>: 배포 브랜치 (직접 push 금지)</li>"
                       + "<li><code>develop</code>: 개발 통합 브랜치</li>"
                       + "<li><code>feature/기능명</code>: 기능별 개발 브랜치</li>"
                       + "</ul>"
                       + "<h3>📝 커밋 메시지 컨벤션</h3>"
                       + "<pre><code>feat: 새로운 기능 추가\nfix: 버그 수정\ndocs: 문서 수정\nrefactor: 리팩토링\ntest: 테스트 코드\nchore: 빌드/설정 변경</code></pre>"
                       + "<h3>🔍 PR 리뷰 규칙</h3>"
                       + "<ul><li>최소 1명 Approve 필요</li><li>셀프 머지 금지</li><li>리뷰 요청 후 24시간 이내 리뷰</li></ul>")
                .author(kim)
                .category("정보")
                .viewCount(423)
                .likeCount(71)
                .build());

        Post p11 = postRepository.save(Post.builder()
                .title("AWS EC2 + RDS + S3 아키텍처 구성기")
                .content("<h2>AWS로 서비스 아키텍처 구성하기</h2>"
                       + "<p>사이드 프로젝트를 AWS에 올리면서 배운 것들을 정리했습니다.</p>"
                       + "<img src=\"https://picsum.photos/seed/aws/800/400\" style=\"max-width:100%;border-radius:8px;margin:12px 0\">"
                       + "<h3>📐 아키텍처 구성</h3>"
                       + "<ul>"
                       + "<li><strong>EC2 t3.micro</strong>: Spring Boot 애플리케이션 서버</li>"
                       + "<li><strong>RDS MySQL</strong>: 운영 데이터베이스 (프리티어)</li>"
                       + "<li><strong>S3</strong>: 정적 파일 (이미지, CSS, JS) 저장</li>"
                       + "<li><strong>CloudFront</strong>: S3 앞단 CDN</li>"
                       + "</ul>"
                       + "<h3>💰 비용 최적화 팁</h3>"
                       + "<p>프리티어 한도 안에서 운영하려면 EC2 t3.micro + RDS t3.micro 조합이 핵심입니다. CloudWatch 알람으로 비용 모니터링 필수!</p>")
                .author(choi)
                .category("정보")
                .viewCount(334)
                .likeCount(55)
                .build());

        Post p12 = postRepository.save(Post.builder()
                .title("AI 코딩 도구 총정리 — GitHub Copilot vs Cursor vs Claude")
                .content("<h2>개발자 AI 도구 2026년 총정리</h2>"
                       + "<p>요즘 AI 코딩 도구 없이는 개발 못 하겠다는 분들 많죠? 직접 써보고 비교해봤습니다.</p>"
                       + "<h3>🤖 GitHub Copilot</h3>"
                       + "<p>IDE 통합이 가장 자연스럽습니다. 코드 자동완성에 특화. 기업 사용 시 코드 유출 우려가 있어 사내 정책 확인 필요.</p>"
                       + "<h3>🖱️ Cursor</h3>"
                       + "<p>VS Code 기반 AI 특화 IDE. 채팅으로 코드베이스 전체를 맥락으로 대화 가능. 대규모 리팩토링에 강점.</p>"
                       + "<h3>🧠 Claude API (직접 연동)</h3>"
                       + "<p>API로 직접 워크플로우에 통합하면 맞춤형 자동화 가능. 긴 컨텍스트 처리 능력이 뛰어남.</p>"
                       + "<h3>💡 결론</h3>"
                       + "<p>일상 코딩은 <strong>Copilot</strong>, 큰 작업은 <strong>Cursor</strong>, 자동화는 <strong>Claude API</strong>로 용도를 나눠 쓰는 것을 추천합니다.</p>")
                .author(jung)
                .category("정보")
                .viewCount(612)
                .likeCount(103)
                .build());

        // ──────────── 댓글 (10개) ────────────
        commentRepository.save(Comment.builder()
                .content("정말 유익한 정리 감사합니다! 저도 처음에 의존성 주입이 제일 헷갈렸어요.")
                .author(park).post(p1).build());
        commentRepository.save(Comment.builder()
                .content("Fetch Join + @BatchSize 조합 써보세요! 페이징도 되고 N+1도 해결됩니다.")
                .author(kim).post(p3).build());
        commentRepository.save(Comment.builder()
                .content("맞아요, @BatchSize(size=100) 설정하면 IN 쿼리로 묶여서 훨씬 낫더라고요.")
                .author(choi).post(p3).build());
        commentRepository.save(Comment.builder()
                .content("스터디 참여하고 싶습니다! 백준 실버2인데 가능할까요?")
                .author(jung).post(p4).build());
        commentRepository.save(Comment.builder()
                .content("React Query 처음엔 낯설었는데 익숙해지면 진짜 편해요. 강추!")
                .author(lee).post(p5).build());
        commentRepository.save(Comment.builder()
                .content("Zustand 쓰다가 Jotai로 갔는데 둘 다 좋은 것 같아요.")
                .author(jung).post(p5).build());
        commentRepository.save(Comment.builder()
                .content("오픈 축하드립니다! 정말 필요했던 플랫폼이에요 🎉")
                .author(kim).post(p6).build());
        commentRepository.save(Comment.builder()
                .content("비전공자 합격 후기 정말 감사해요. 저도 지금 준비 중인데 많이 도움됐습니다!")
                .author(lee).post(p7).build());
        commentRepository.save(Comment.builder()
                .content("docker-compose 예시 혹시 공유해주실 수 있나요?")
                .author(park).post(p8).build());
        commentRepository.save(Comment.builder()
                .content("Cursor 써봤는데 진짜 신세계예요. 특히 Codebase chat 기능이 최고!")
                .author(choi).post(p12).build());

        // ──────────── 기술 문서 (6개, HTML 포맷) ────────────
        techDocRepository.save(TechDoc.builder()
                .title("Spring Boot 입문 — 나만의 첫 REST API 만들기")
                .content("<h2>Spring Boot REST API 입문</h2>"
                       + "<p>Spring Initializr에서 Web, JPA, H2 의존성을 선택해 프로젝트를 만드는 것부터 시작합니다.</p>"
                       + "<img src=\"https://picsum.photos/seed/springboot/800/350\" style=\"max-width:100%;border-radius:8px;margin:12px 0\">"
                       + "<h3>엔티티 정의</h3>"
                       + "<pre><code>@Entity\npublic class Item {\n  @Id @GeneratedValue\n  private Long id;\n  private String name;\n}</code></pre>"
                       + "<h3>Repository</h3>"
                       + "<pre><code>public interface ItemRepository extends JpaRepository&lt;Item, Long&gt; {}</code></pre>"
                       + "<h3>Controller</h3>"
                       + "<pre><code>@RestController\n@RequestMapping(\"/api/items\")\npublic class ItemController {\n  @GetMapping public List&lt;Item&gt; list() { return repo.findAll(); }\n}</code></pre>"
                       + "<p>이것만으로 동작하는 CRUD API가 완성됩니다! 🎉</p>")
                .author(admin).category("튜토리얼").tags("Spring Boot,REST API,JPA,입문")
                .viewCount(680).pinned(true).build());

        techDocRepository.save(TechDoc.builder()
                .title("JPA 연관관계 완전 정복 — N+1 해결까지")
                .content("<h2>JPA 연관관계 가이드</h2>"
                       + "<p>JPA에서 가장 많이 실수하는 연관관계 설정과 N+1 문제 해결법을 정리합니다.</p>"
                       + "<h3>단방향 vs 양방향</h3>"
                       + "<p>연관관계는 가능하면 <strong>단방향</strong>으로 설계합니다. 양방향은 편의 메서드 관리가 필요해 복잡도가 높아집니다.</p>"
                       + "<h3>Fetch 전략</h3>"
                       + "<ul><li><strong>LAZY</strong> (권장): 실제 사용 시점에 SELECT</li>"
                       + "<li><strong>EAGER</strong> (주의): 즉시 JOIN SELECT — N+1 위험</li></ul>"
                       + "<h3>N+1 해결법</h3>"
                       + "<ol><li>JPQL Fetch Join</li><li>@EntityGraph</li><li>@BatchSize(size=100)</li></ol>"
                       + "<p><strong>페이징 + Fetch Join</strong>은 함께 쓸 수 없습니다. 이 경우 @BatchSize를 사용하세요.</p>")
                .author(kim).category("데이터베이스").tags("JPA,연관관계,N+1,Hibernate")
                .viewCount(912).pinned(true).build());

        techDocRepository.save(TechDoc.builder()
                .title("알고리즘 유형별 완전 정복 — BFS/DFS/DP")
                .content("<h2>알고리즘 유형별 접근 패턴</h2>"
                       + "<p>코딩 테스트에서 자주 나오는 유형별 접근법을 정리합니다.</p>"
                       + "<h3>BFS (너비 우선 탐색)</h3>"
                       + "<p>최단 경로, 레벨 탐색에 사용합니다.</p>"
                       + "<pre><code>Queue&lt;int[]&gt; q = new LinkedList&lt;&gt;();\nq.offer(new int[]{startX, startY});\nwhile (!q.isEmpty()) {\n  int[] cur = q.poll();\n  // 상하좌우 탐색\n}</code></pre>"
                       + "<h3>DFS (깊이 우선 탐색)</h3>"
                       + "<p>모든 경우의 수 탐색, 순열/조합 생성에 사용합니다.</p>"
                       + "<pre><code>void dfs(int depth) {\n  if (depth == N) { count++; return; }\n  for (int i = 0; i &lt; N; i++) {\n    if (!visited[i]) { visited[i]=true; dfs(depth+1); visited[i]=false; }\n  }\n}</code></pre>"
                       + "<h3>DP (동적 프로그래밍)</h3>"
                       + "<p>점화식을 먼저 세우고 Top-down(메모이제이션) 또는 Bottom-up(타뷸레이션)으로 구현합니다.</p>")
                .author(lee).category("알고리즘").tags("BFS,DFS,DP,알고리즘,코딩테스트")
                .viewCount(756).pinned(false).build());

        techDocRepository.save(TechDoc.builder()
                .title("React 18 핵심 정리 — Concurrent Features & Hooks")
                .content("<h2>React 18의 핵심 변경점</h2>"
                       + "<p>React 18에서 도입된 Concurrent 기능들과 새 Hooks를 정리합니다.</p>"
                       + "<h3>Concurrent Rendering</h3>"
                       + "<p><code>startTransition</code>으로 긴급하지 않은 상태 업데이트를 표시해 UI 반응성을 유지합니다.</p>"
                       + "<pre><code>startTransition(() =&gt; setState(newValue));</code></pre>"
                       + "<h3>useTransition</h3>"
                       + "<pre><code>const [isPending, startTransition] = useTransition();</code></pre>"
                       + "<h3>useDeferredValue</h3>"
                       + "<p>값의 업데이트를 지연시켜 검색 입력 등에서 타이핑 렉을 방지합니다.</p>"
                       + "<h3>Suspense 개선</h3>"
                       + "<p>서버 컴포넌트와 함께 스트리밍 SSR을 지원합니다.</p>"
                       + "<ul><li>컴포넌트 단위로 로딩 경계를 설정</li><li>데이터 페칭 라이브러리(React Query 등)와 통합</li></ul>")
                .author(park).category("프론트엔드").tags("React,React18,Hooks,Concurrent,프론트엔드")
                .viewCount(534).pinned(false).build());

        techDocRepository.save(TechDoc.builder()
                .title("Docker & Docker Compose 실전 가이드")
                .content("<h2>Docker로 Spring Boot 앱 배포하기</h2>"
                       + "<img src=\"https://picsum.photos/seed/docker/800/300\" style=\"max-width:100%;border-radius:8px;margin:12px 0\">"
                       + "<h3>Dockerfile 작성</h3>"
                       + "<pre><code>FROM openjdk:21-slim\nARG JAR_FILE=build/libs/*.jar\nCOPY ${JAR_FILE} app.jar\nENTRYPOINT [\"java\",\"-jar\",\"/app.jar\"]</code></pre>"
                       + "<h3>docker-compose.yml</h3>"
                       + "<pre><code>services:\n  app:\n    build: .\n    ports:\n      - \"8080:8080\"\n    env_file: .env\n  db:\n    image: mysql:8\n    environment:\n      MYSQL_ROOT_PASSWORD: root</code></pre>"
                       + "<h3>자주 쓰는 명령어</h3>"
                       + "<ul><li><code>docker-compose up -d</code> — 백그라운드 실행</li>"
                       + "<li><code>docker-compose logs -f</code> — 로그 실시간 확인</li>"
                       + "<li><code>docker exec -it &lt;id&gt; bash</code> — 컨테이너 접속</li></ul>")
                .author(choi).category("DevOps").tags("Docker,Docker Compose,DevOps,배포,EC2")
                .viewCount(612).pinned(false).build());

        techDocRepository.save(TechDoc.builder()
                .title("CS 면접 필수 암기 — 운영체제·네트워크 총정리")
                .content("<h2>개발자 기술 면접 CS 핵심 정리</h2>"
                       + "<p>SSAFY 수료 후 취업 면접에서 자주 나오는 CS 질문들을 정리했습니다.</p>"
                       + "<h3>운영체제</h3>"
                       + "<ul>"
                       + "<li><strong>프로세스 vs 스레드:</strong> 프로세스는 독립 메모리, 스레드는 메모리 공유</li>"
                       + "<li><strong>교착상태(Deadlock) 조건:</strong> 상호배제, 점유대기, 비선점, 순환대기 모두 성립 시 발생</li>"
                       + "<li><strong>페이징:</strong> 고정 크기 블록으로 메모리 분할, 외부 단편화 없음</li>"
                       + "</ul>"
                       + "<h3>네트워크</h3>"
                       + "<ul>"
                       + "<li><strong>TCP vs UDP:</strong> TCP는 신뢰성(3-way handshake), UDP는 빠른 전송</li>"
                       + "<li><strong>HTTP vs HTTPS:</strong> HTTPS는 TLS 암호화 적용</li>"
                       + "<li><strong>REST API:</strong> Stateless, 자원 중심 URI, HTTP 메서드 활용</li>"
                       + "</ul>")
                .author(jung).category("기타").tags("CS,면접,운영체제,네트워크,취준")
                .viewCount(1120).pinned(true).build());

        // ──────────── 기술 영상 (5개) ────────────
        techVideoRepository.save(TechVideo.builder()
                .title("Spring Boot 3.x + JPA 실전 강의 풀버전")
                .description("Spring Boot 3.x와 Spring Data JPA를 활용한 실전 프로젝트 개발 강의입니다. 엔티티 설계부터 REST API 완성, 배포까지 전 과정을 다룹니다.")
                .youtubeId("9SGDpanrc8U").duration("2:14:30")
                .author(admin).category("강의").tags("Spring Boot,JPA,REST API,백엔드")
                .viewCount(2140).pinned(true).build());

        techVideoRepository.save(TechVideo.builder()
                .title("백준 BFS/DFS 유형 완전 정복 — 실전 문제풀이")
                .description("BFS와 DFS를 활용한 백준 알고리즘 문제 풀이 시리즈입니다. 기초 개념부터 응용 문제까지 단계별로 설명하며, 자주 나오는 패턴을 정리합니다.")
                .youtubeId("7C9RgOcvkvo").duration("45:20")
                .author(lee).category("강의").tags("알고리즘,BFS,DFS,백준,코딩테스트")
                .viewCount(1456).pinned(true).build());

        techVideoRepository.save(TechVideo.builder()
                .title("SSAFYnity 프로젝트 발표 — 설계부터 배포까지")
                .description("SSAFY 최종 프로젝트로 개발한 커뮤니티 플랫폼 SSAFYnity의 아키텍처 설계, 개발 과정, 트러블슈팅 경험을 공유합니다.")
                .youtubeId("dQw4w9WgXcQ").duration("18:45")
                .author(park).category("프로젝트발표").tags("Spring Boot,Thymeleaf,Redis,프로젝트발표")
                .viewCount(734).pinned(false).build());

        techVideoRepository.save(TechVideo.builder()
                .title("실무 코드 리뷰 — Spring Boot 코드 품질 개선하기")
                .description("실제 Spring Boot 코드를 리뷰하면서 SOLID 원칙, 테스트 코드 작성, 리팩토링 방법을 실습합니다. 나쁜 코드를 좋은 코드로 바꾸는 과정을 직접 보여줍니다.")
                .youtubeId("jNQXAC9IVRw").duration("38:10")
                .author(kim).category("코드리뷰").tags("Spring Boot,코드리뷰,리팩토링,SOLID,클린코드")
                .viewCount(923).pinned(false).build());

        techVideoRepository.save(TechVideo.builder()
                .title("AWS 무중단 배포 구성 — EC2 + RDS + S3 + CodeDeploy")
                .description("AWS 서비스를 조합해 Spring Boot 애플리케이션을 무중단 배포하는 환경을 구성하는 실습 영상입니다. Blue/Green 배포 전략을 직접 적용해봅니다.")
                .youtubeId("fJ9rUzIMcZQ").duration("52:30")
                .author(choi).category("강의").tags("AWS,EC2,S3,CodeDeploy,무중단배포,DevOps")
                .viewCount(1087).pinned(false).build());

        // ──────────── 이벤트 (4개, HTML 포맷) ────────────
        eventRepository.save(Event.builder()
                .title("Spring Boot & JPA 집중 스터디 5기 모집")
                .description("<h2>Spring Boot + JPA 집중 스터디 5기</h2>"
                           + "<p>Spring Boot와 JPA를 함께 공부하는 <strong>온라인 스터디</strong>입니다. 매주 토요일 오전 10시, 4주간 진행합니다.</p>"
                           + "<h3>📚 커리큘럼</h3>"
                           + "<ul><li><strong>1주차:</strong> Spring Boot 기초 + REST API 설계</li>"
                           + "<li><strong>2주차:</strong> JPA 엔티티 설계 + 연관관계</li>"
                           + "<li><strong>3주차:</strong> QueryDSL + 페이징 + 성능 최적화</li>"
                           + "<li><strong>4주차:</strong> 실전 미니 프로젝트 완성</li></ul>"
                           + "<h3>📋 지원 조건</h3>"
                           + "<p>Java 기초 문법 이해 / 매주 2~3시간 투자 가능 / 과제 제출 필수</p>")
                .startDate(LocalDateTime.of(2026, 4, 12, 10, 0))
                .endDate(LocalDateTime.of(2026, 5, 3, 12, 0))
                .location("ONLINE").eventType("스터디").organizer(kim)
                .maxParticipants(20).currentParticipants(13).status("UPCOMING").build());

        eventRepository.save(Event.builder()
                .title("SSAFY 해커톤 2026 — AI 서비스 개발 챌린지")
                .description("<h2>🔥 SSAFY 해커톤 2026</h2>"
                           + "<p>SSAFY 구성원이 팀을 구성해 <strong>48시간 동안 AI 서비스를 개발</strong>하는 해커톤입니다.</p>"
                           + "<img src=\"https://picsum.photos/seed/hackathon/800/350\" style=\"max-width:100%;border-radius:8px;margin:12px 0\">"
                           + "<h3>🏆 시상 내역</h3>"
                           + "<ul><li>대상 (1팀): 상금 100만원 + 트로피</li>"
                           + "<li>최우수상 (2팀): 상금 50만원</li>"
                           + "<li>우수상 (3팀): 상금 30만원</li></ul>"
                           + "<h3>📌 참가 규정</h3>"
                           + "<ul><li>팀 구성: 3~5인</li>"
                           + "<li>주제: AI를 활용한 일상 문제 해결</li>"
                           + "<li>사용 언어/프레임워크 자유</li></ul>")
                .startDate(LocalDateTime.of(2026, 5, 1, 18, 0))
                .endDate(LocalDateTime.of(2026, 5, 3, 18, 0))
                .location("OFFLINE").eventType("해커톤").organizer(admin)
                .maxParticipants(100).currentParticipants(61).status("UPCOMING").build());

        eventRepository.save(Event.builder()
                .title("월간 코드 리뷰 세션 — 4월호")
                .description("<h2>💻 월간 코드 리뷰 세션</h2>"
                           + "<p>매달 진행하는 코드 리뷰 세션입니다. 참가자가 자신의 코드를 공유하고 건설적인 피드백을 주고받습니다.</p>"
                           + "<h3>이번 달 주제</h3>"
                           + "<p><strong>Spring Boot API 서버 — 성능 최적화 코드 리뷰</strong></p>"
                           + "<h3>진행 방식</h3>"
                           + "<ul><li>Zoom 화상회의 (링크는 당일 공지)</li>"
                           + "<li>참가자 코드 사전 공유 (행사 3일 전까지)</li>"
                           + "<li>1인당 15분씩 코드 발표 + 피드백</li></ul>")
                .startDate(LocalDateTime.of(2026, 4, 24, 20, 0))
                .endDate(LocalDateTime.of(2026, 4, 24, 22, 0))
                .location("ONLINE").eventType("세미나").organizer(park)
                .maxParticipants(15).currentParticipants(8).status("UPCOMING").build());

        eventRepository.save(Event.builder()
                .title("SSAFY 취업 준비 세미나 — 이력서·포트폴리오 클리닉")
                .description("<h2>📄 취업 준비 세미나</h2>"
                           + "<p>SSAFY 수료 후 취업에 성공한 선배 개발자들이 이력서와 포트폴리오 클리닉을 진행합니다.</p>"
                           + "<h3>프로그램</h3>"
                           + "<ul>"
                           + "<li><strong>1부 (1시간):</strong> 합격 이력서 실제 사례 분석</li>"
                           + "<li><strong>2부 (1시간):</strong> 포트폴리오 GitHub 정리법</li>"
                           + "<li><strong>3부 (30분):</strong> 개인 이력서 피드백 (선착순 10명)</li>"
                           + "</ul>"
                           + "<p>참가 신청은 선착순이며 마감 시 대기 신청도 받습니다.</p>")
                .startDate(LocalDateTime.of(2026, 3, 21, 14, 0))
                .endDate(LocalDateTime.of(2026, 3, 21, 17, 0))
                .location("OFFLINE").eventType("세미나").organizer(admin)
                .maxParticipants(50).currentParticipants(50).status("ONGOING").build());

        // ──────────── 프로젝트 (5개, HTML 포맷) ────────────
        projectRepository.save(Project.builder()
                .title("SSAFYnity — SSAFY 통합 커뮤니티 플랫폼")
                .description("<h2>SSAFYnity 프로젝트 소개</h2>"
                           + "<p>SSAFY 구성원들을 위한 <strong>통합 커뮤니티 플랫폼</strong>입니다. 기술 공유, 이벤트 모집, 프로젝트 쇼케이스를 한 곳에서 제공합니다.</p>"
                           + "<img src=\"https://picsum.photos/seed/ssafynity/800/400\" style=\"max-width:100%;border-radius:8px;margin:12px 0\">"
                           + "<h3>🚀 주요 기능</h3>"
                           + "<ul>"
                           + "<li>📝 커뮤니티 게시판 (카테고리·검색·좋아요)</li>"
                           + "<li>📚 기술 문서 공유 & 북마크</li>"
                           + "<li>🎬 교육 영상 아카이브</li>"
                           + "<li>📅 이벤트·스터디 모집</li>"
                           + "<li>🏆 프로젝트 쇼케이스</li>"
                           + "<li>💬 Redis Pub/Sub 실시간 채팅</li>"
                           + "</ul>"
                           + "<h3>🛠️ 기술적 도전</h3>"
                           + "<p>QueryDSL을 활용한 동적 쿼리, Redis를 이용한 실시간 채팅, Thymeleaf Layout Dialect를 이용한 템플릿 재사용 구조를 구현했습니다.</p>")
                .techStack("Spring Boot,Spring Data JPA,QueryDSL,Thymeleaf,Redis,H2,Gradle")
                .githubUrl("https://github.com/ssafynity/ssafynity")
                .demoUrl("http://localhost:8080")
                .thumbnailUrl("https://picsum.photos/seed/proj1/600/340")
                .author(park).teamSize(4).status("IN_PROGRESS")
                .likeCount(78).viewCount(534).build());

        projectRepository.save(Project.builder()
                .title("AlgoTracker — 알고리즘 학습 진도 관리 앱")
                .description("<h2>AlgoTracker 소개</h2>"
                           + "<p>백준, 프로그래머스 문제 풀이 현황을 시각화하는 <strong>학습 진도 관리 앱</strong>입니다.</p>"
                           + "<img src=\"https://picsum.photos/seed/algo2/800/350\" style=\"max-width:100%;border-radius:8px;margin:12px 0\">"
                           + "<h3>핵심 기능</h3>"
                           + "<ul>"
                           + "<li>📊 문제 태그별 통계 차트 (Chart.js)</li>"
                           + "<li>🔥 스트릭(연속 풀이) 히트맵 시각화</li>"
                           + "<li>❌ 틀린 문제 오답노트 & 재풀이 알림</li>"
                           + "<li>👥 친구 추가 후 진도 비교</li>"
                           + "</ul>"
                           + "<h3>개발 이유</h3>"
                           + "<p>코딩 테스트 준비를 하다 보니 내가 어떤 유형이 약한지 파악이 안 돼서 직접 만들었습니다. GitHub 잔디처럼 알고리즘 풀이도 시각화하고 싶었어요!</p>")
                .techStack("React,TypeScript,Node.js,Express,PostgreSQL,Chart.js")
                .githubUrl("https://github.com/lee_ssafy/algotracker")
                .thumbnailUrl("https://picsum.photos/seed/proj2/600/340")
                .author(lee).teamSize(2).status("COMPLETED")
                .likeCount(51).viewCount(312).build());

        projectRepository.save(Project.builder()
                .title("DevDiary — 개발자 일일 회고 서비스")
                .description("<h2>DevDiary 프로젝트</h2>"
                           + "<p>매일 개발 학습을 기록하고 공유하는 <strong>회고 서비스</strong>입니다. GitHub Actions로 매일 밤 12시에 회고 작성 알림을 보냅니다.</p>"
                           + "<h3>특징</h3>"
                           + "<ul>"
                           + "<li>✏️ WYSIWYG 리치텍스트 에디터</li>"
                           + "<li>🗓️ 월별 회고 달력 뷰</li>"
                           + "<li>🔒 공개/비공개 선택</li>"
                           + "<li>🤖 GitHub Actions 자동 알림</li>"
                           + "</ul>")
                .techStack("Spring Boot,React,MySQL,Docker,GitHub Actions,Quill.js")
                .githubUrl("https://github.com/kim_dev/devdiary")
                .demoUrl("https://devdiary.example.com")
                .thumbnailUrl("https://picsum.photos/seed/proj3/600/340")
                .author(kim).teamSize(1).status("COMPLETED")
                .likeCount(63).viewCount(445).build());

        projectRepository.save(Project.builder()
                .title("InterviewPrepKit — 기술 면접 준비 도구")
                .description("<h2>InterviewPrepKit</h2>"
                           + "<p>개발자 기술 면접을 체계적으로 준비할 수 있는 <strong>학습 도구</strong>입니다. SSAFY 수료 후 직접 취업 준비하면서 만들었습니다.</p>"
                           + "<h3>포함 콘텐츠</h3>"
                           + "<ul>"
                           + "<li>🧠 CS 기초 질문 500+ (자료구조, OS, 네트워크, DB)</li>"
                           + "<li>☕ Spring/Java 면접 질문 200+</li>"
                           + "<li>⏱️ 모의 면접 타이머 기능</li>"
                           + "<li>💾 답변 저장 & 복습 스케줄링</li>"
                           + "</ul>"
                           + "<p>현재 Vue.js 기반이지만 React로 마이그레이션 계획 중입니다.</p>")
                .techStack("Vue.js,Spring Boot,H2,Bootstrap,Spring Security")
                .githubUrl("https://github.com/park_full/interview-prep-kit")
                .thumbnailUrl("https://picsum.photos/seed/proj4/600/340")
                .author(park).teamSize(3).status("COMPLETED")
                .likeCount(112).viewCount(723).build());

        projectRepository.save(Project.builder()
                .title("CloudMonitor — 클라우드 비용 모니터링 대시보드")
                .description("<h2>CloudMonitor 소개</h2>"
                           + "<p>AWS 멀티 계정의 <strong>클라우드 비용을 실시간으로 모니터링</strong>하는 대시보드입니다. Cost Explorer API를 연동해 서비스별 비용을 시각화합니다.</p>"
                           + "<img src=\"https://picsum.photos/seed/cloud/800/350\" style=\"max-width:100%;border-radius:8px;margin:12px 0\">"
                           + "<h3>주요 기능</h3>"
                           + "<ul>"
                           + "<li>📈 일/월별 AWS 비용 추이 차트</li>"
                           + "<li>🚨 임계값 초과 시 Slack 알림</li>"
                           + "<li>🏷️ 서비스별 비용 분류 (EC2, RDS, S3 등)</li>"
                           + "<li>📧 주간 비용 리포트 이메일 발송</li>"
                           + "</ul>")
                .techStack("Spring Boot,React,AWS SDK,Chart.js,Docker,Slack API")
                .githubUrl("https://github.com/choi_cloud/cloud-monitor")
                .demoUrl("https://cloudmonitor.example.com")
                .thumbnailUrl("https://picsum.photos/seed/proj5/600/340")
                .author(choi).teamSize(2).status("IN_PROGRESS")
                .likeCount(44).viewCount(278).build());

        // ──────────── 알림 (10개) ────────────
        // 댓글 알림
        notificationRepository.save(Notification.builder()
                .receiver(park).message("🔔 김개발님이 내 게시글에 댓글을 달았습니다: \"정말 유익한 정리 감사합니다!...\"")
                .link("/posts/1").isRead(false).build());
        notificationRepository.save(Notification.builder()
                .receiver(lee).message("🔔 김개발님이 내 게시글에 댓글을 달았습니다: \"Fetch Join + @BatchSize 조합 써보세요!...\"")
                .link("/posts/3").isRead(false).build());
        notificationRepository.save(Notification.builder()
                .receiver(lee).message("🔔 최클라우드님이 내 게시글에 댓글을 달았습니다: \"맞아요, @BatchSize(size=100) 설정하면...\"")
                .link("/posts/3").isRead(true).build());
        notificationRepository.save(Notification.builder()
                .receiver(lee).message("🔔 정AI님이 내 게시글에 댓글을 달았습니다: \"스터디 참여하고 싶습니다!...\"")
                .link("/posts/4").isRead(false).build());
        notificationRepository.save(Notification.builder()
                .receiver(park).message("🔔 이싸피님이 내 게시글에 댓글을 달았습니다: \"React Query 처음엔 낯설었는데...\"")
                .link("/posts/5").isRead(true).build());
        notificationRepository.save(Notification.builder()
                .receiver(admin).message("🔔 김개발님이 내 게시글에 댓글을 달았습니다: \"오픈 축하드립니다! 🎉\"")
                .link("/posts/6").isRead(true).build());
        notificationRepository.save(Notification.builder()
                .receiver(jung).message("🔔 이싸피님이 내 게시글에 댓글을 달았습니다: \"비전공자 합격 후기 정말 감사해요...\"")
                .link("/posts/7").isRead(false).build());
        notificationRepository.save(Notification.builder()
                .receiver(choi).message("🔔 박풀스택님이 내 게시글에 댓글을 달았습니다: \"docker-compose 예시 공유 가능한가요?...\"")
                .link("/posts/8").isRead(false).build());
        // 이벤트/프로젝트 알림
        notificationRepository.save(Notification.builder()
                .receiver(kim).message("📅 주최하신 스터디에 새 참가자가 신청했습니다. 현재 13 / 20명")
                .link("/events/1").isRead(false).build());
        notificationRepository.save(Notification.builder()
                .receiver(park).message("🏆 SSAFYnity 프로젝트에 새 좋아요가 추가됐습니다!")
                .link("/projects/1").isRead(true).build());

        // ──────────── 북마크 (8개) ────────────
        bookmarkRepository.save(Bookmark.builder()
                .member(kim).targetType("POST").targetId(6L)
                .targetTitle("[공지] SSAFYnity 플랫폼 정식 오픈!").build());
        bookmarkRepository.save(Bookmark.builder()
                .member(kim).targetType("DOC").targetId(2L)
                .targetTitle("JPA 연관관계 완전 정복 — N+1 해결까지").build());
        bookmarkRepository.save(Bookmark.builder()
                .member(kim).targetType("VIDEO").targetId(1L)
                .targetTitle("Spring Boot 3.x + JPA 실전 강의 풀버전").build());
        bookmarkRepository.save(Bookmark.builder()
                .member(lee).targetType("POST").targetId(8L)
                .targetTitle("Docker + Spring Boot 배포 완전 정복 — 삽질 기록").build());
        bookmarkRepository.save(Bookmark.builder()
                .member(lee).targetType("DOC").targetId(3L)
                .targetTitle("알고리즘 유형별 완전 정복 — BFS/DFS/DP").build());
        bookmarkRepository.save(Bookmark.builder()
                .member(park).targetType("DOC").targetId(6L)
                .targetTitle("CS 면접 필수 암기 — 운영체제·네트워크 총정리").build());
        bookmarkRepository.save(Bookmark.builder()
                .member(park).targetType("VIDEO").targetId(2L)
                .targetTitle("백준 BFS/DFS 유형 완전 정복 — 실전 문제풀이").build());
        bookmarkRepository.save(Bookmark.builder()
                .member(jung).targetType("POST").targetId(11L)
                .targetTitle("AWS EC2 + RDS + S3 아키텍처 구성기").build());

        // ──────────── 멘토 프로필 (4명) ────────────
        MentorProfile mentorAdmin = mentorProfileRepository.save(MentorProfile.builder()
                .member(admin)
                .title("Spring Boot & JPA 아키텍처 멘토링")
                .career("SSAFY 운영진 출신, 백엔드 개발 7년차, 현 플랫폼 팀 선임 개발자")
                .specialties("Spring Boot,Spring Security,JPA,QueryDSL,MSA,시스템 설계")
                .mentorBio("안녕하세요! SSAFYnity 운영을 맡고 있는 관리자입니다.\n\n"
                         + "백엔드 개발 7년차로, Spring 생태계를 깊이 있게 다뤄왔습니다. "
                         + "주로 엔터프라이즈 레벨의 서비스 설계와 JPA 성능 최적화 경험이 풍부합니다.\n\n"
                         + "멘토링 방식: 매주 1회 화상 미팅 + 코드 리뷰 + 슬랙 Q&A\n"
                         + "주로 다루는 주제: N+1 해결, 인덱스 최적화, Spring Security 설계, MSA 입문")
                .maxMentees(3)
                .currentMentees(1)
                .sessionCount(28)
                .active(true)
                .build());

        MentorProfile mentorPark = mentorProfileRepository.save(MentorProfile.builder()
                .member(park)
                .title("React + Spring Boot 풀스택 개발 멘토링")
                .career("SSAFY 10기 수료, 현 스타트업 풀스택 개발자 1.5년차")
                .specialties("React,TypeScript,Spring Boot,REST API,Git,프로젝트 설계")
                .mentorBio("풀스택 개발자로 일하면서 쌓은 실무 노하우를 나눠드리고 싶습니다!\n\n"
                         + "SSAFY 최종 프로젝트 우수상 수상 경험이 있고, 현재 스타트업에서 "
                         + "프론트엔드(React/TypeScript)와 백엔드(Spring Boot)를 동시에 담당하고 있습니다.\n\n"
                         + "특히 취업 준비생들을 위한 포트폴리오 프로젝트 설계와 코드 리뷰에 집중합니다.\n"
                         + "같이 프로젝트를 만들어가는 방식으로 진행합니다!")
                .maxMentees(5)
                .currentMentees(2)
                .sessionCount(15)
                .active(true)
                .build());

        MentorProfile mentorChoi = mentorProfileRepository.save(MentorProfile.builder()
                .member(choi)
                .title("AWS & Docker DevOps 실전 멘토링")
                .career("SSAFY 11기 수료, Cloud/DevOps 엔지니어 1년차, AWS SAA 자격증 보유")
                .specialties("AWS,Docker,Docker Compose,CI/CD,Linux,GitHub Actions,Kubernetes 입문")
                .mentorBio("DevOps 엔지니어로 일하면서 배포와 인프라의 재미에 빠졌습니다 😄\n\n"
                         + "AWS EC2/RDS/S3/CloudFront 구성부터 Docker 컨테이너 배포, "
                         + "GitHub Actions CI/CD 파이프라인 구축까지 실무 경험을 공유합니다.\n\n"
                         + "멘토링 주제: 사이드 프로젝트 AWS 배포, Docker 입문, "
                         + "CI/CD 파이프라인 설계, 비용 최적화 팁")
                .maxMentees(4)
                .currentMentees(0)
                .sessionCount(8)
                .active(true)
                .build());

        MentorProfile mentorLee = mentorProfileRepository.save(MentorProfile.builder()
                .member(lee)
                .title("코딩 테스트 & 알고리즘 집중 멘토링")
                .career("SSAFY 11기 수료, PS 대회 입상, 백준 Diamond 달성")
                .specialties("알고리즘,BFS/DFS,DP,그리디,백준,프로그래머스,코딩테스트")
                .mentorBio("알고리즘 공부를 열정적으로 해온 이싸피입니다!\n\n"
                         + "백준 Diamond 티어, 프로그래머스 Lv.4 다수 풀이 경험이 있습니다. "
                         + "코딩 테스트로 어려움을 겪고 있는 분들을 집중 지원합니다.\n\n"
                         + "진행 방식: 취약 유형 분석 → 유형별 집중 풀이 → 실전 모의 테스트\n"
                         + "목표: 2~3개월 내 삼성/카카오/네이버 코테 통과 수준 달성")
                .maxMentees(6)
                .currentMentees(3)
                .sessionCount(42)
                .active(true)
                .build());

        // ──────────── 멘토링 신청 (더미, 다양한 상태) ────────────

        // kim → mentorLee 에게 신청 (PENDING)
        mentoringRequestRepository.save(MentoringRequest.builder()
                .mentee(kim)
                .mentorProfile(mentorLee)
                .message("안녕하세요! 코딩 테스트 공부를 시작한 지 3개월째인데 "
                       + "아직 프로그래머스 Lv.2 문제도 어렵게 느껴집니다. "
                       + "BFS/DFS 유형이 특히 취약한데 집중적으로 도움받고 싶습니다!")
                .status("PENDING")
                .build());

        // jung → mentorAdmin 에게 신청 (PENDING)
        mentoringRequestRepository.save(MentoringRequest.builder()
                .mentee(jung)
                .mentorProfile(mentorAdmin)
                .message("Spring Boot로 사이드 프로젝트를 진행 중인데, JPA를 쓰다 보니 "
                       + "N+1 문제와 성능 이슈가 자주 발생합니다. "
                       + "실무 관점에서 JPA 최적화 노하우를 배우고 싶습니다.")
                .status("PENDING")
                .build());

        // lee → mentorPark 에게 신청 (PENDING)
        mentoringRequestRepository.save(MentoringRequest.builder()
                .mentee(lee)
                .mentorProfile(mentorPark)
                .message("백엔드 위주로 개발해왔는데 프론트엔드(React)도 공부하고 싶어요. "
                       + "풀스택으로 포트폴리오를 만들고 싶은데 어디서부터 시작해야 할지 "
                       + "방향을 잡아주시면 감사하겠습니다!")
                .status("PENDING")
                .build());

        // choi → mentorLee 에게 신청 (REJECTED)
        mentoringRequestRepository.save(MentoringRequest.builder()
                .mentee(choi)
                .mentorProfile(mentorLee)
                .message("코딩 테스트 준비를 하고 싶습니다. 현재 골드 수준인데 플래티넘까지 올리고 싶어요.")
                .status("REJECTED")
                .build());

        // ──────────── 캠퍼스 전용 게시글 (각 캠퍼스별 2~3개) ────────────
        postRepository.save(Post.builder()
                .title("[서울] 13기 최종 프로젝트 발표회 후기")
                .content("<h2>서울 13기 최종 프로젝트 발표회 🎉</h2>"
                       + "<p>드디어 최종 프로젝트 발표가 끝났습니다! 6주간 팀원들과 함께한 시간이 정말 소중했어요.</p>"
                       + "<p>우리 팀은 AI 기반 코드 리뷰 자동화 서비스를 개발했는데, 심사위원 분들 반응이 너무 좋았습니다! 서울 13기 모두 수고했어요 👏</p>"
                       + "<img src=\"https://picsum.photos/seed/seoul13/800/400\" style=\"max-width:100%;border-radius:8px;margin:12px 0\">")
                .author(kim).category("캠퍼스").campus("서울")
                .viewCount(47).likeCount(12).build());

        postRepository.save(Post.builder()
                .title("[서울] 캠퍼스 근처 맛집 공유 스레드")
                .content("<h2>서울 캠퍼스 근처 맛집 모음 🍜</h2>"
                       + "<p>점심/저녁 먹을 곳 찾으시는 분들을 위해 자주 가는 맛집들을 정리해봤어요. 댓글로 추가해주세요!</p>"
                       + "<ul><li><strong>설렁탕 집:</strong> 캠퍼스 정문에서 5분, 국물이 진해서 자주 가요</li>"
                       + "<li><strong>일식 덮밥:</strong> 가성비 최고. 점심 특선 9천원</li>"
                       + "<li><strong>카페:</strong> 3층 창가 자리에서 공부하기 딱 좋아요</li></ul>")
                .author(park).category("캠퍼스").campus("서울")
                .viewCount(89).likeCount(23).build());

        postRepository.save(Post.builder()
                .title("[서울] 코딩 스터디 모집 — 매주 화/목 저녁")
                .content("<h2>서울 캠퍼스 코딩 스터디 모집 💻</h2>"
                       + "<p>서울 캠퍼스 분들끼리 알고리즘/코딩 테스트 스터디 하실 분 모십니다!</p>"
                       + "<ul><li>시간: 매주 화, 목 오후 7시</li><li>장소: 캠퍼스 세미나실</li><li>모집 인원: 5~8명</li></ul>"
                       + "<p>참여 희망하시는 분은 댓글 달아주세요 🙌</p>")
                .author(admin).category("캠퍼스").campus("서울")
                .viewCount(62).likeCount(8).build());

        postRepository.save(Post.builder()
                .title("[대전] 13기 화이팅! 중간 프로젝트 마무리 후기")
                .content("<h2>대전 13기 중간 프로젝트 완료! 🏆</h2>"
                       + "<p>2주간의 중간 프로젝트가 드디어 끝났습니다. 대전 캠퍼스 분들 모두 정말 수고 많으셨어요!</p>"
                       + "<p>우리 13기는 정말 팀워크가 끝내줬던 것 같아요. 다들 취업도 파이팅하세요! 💪</p>"
                       + "<img src=\"https://picsum.photos/seed/daejeon13/800/350\" style=\"max-width:100%;border-radius:8px;margin:12px 0\">")
                .author(lee).category("캠퍼스").campus("대전")
                .viewCount(34).likeCount(9).build());

        postRepository.save(Post.builder()
                .title("[대전] 대전 캠퍼스 정보 공유 & 꿀팁 모음")
                .content("<h2>대전 캠퍼스 생존 꿀팁 🌿</h2>"
                       + "<p>대전 새내기분들을 위해 꿀팁을 정리해봤습니다!</p>"
                       + "<ul><li>🚌 버스: 001번 타면 캠퍼스 정문까지 직통</li>"
                       + "<li>🍱 점심: 구내식당이 제일 가성비 좋아요 (6천원)</li>"
                       + "<li>📚 스터디룸: 오전 9시 이후 예약 가능, 미리미리!</li>"
                       + "<li>☕ 카페: 2층 카페 아메리카노 2천원 👍</li></ul>")
                .author(lee).category("캠퍼스").campus("대전")
                .viewCount(58).likeCount(15).build());

        postRepository.save(Post.builder()
                .title("[구미] AWS 특강 후기 + 자료 공유")
                .content("<h2>구미 캠퍼스 AWS 특강 들으셨나요? ⚙️</h2>"
                       + "<p>어제 AWS 특강 정말 유익했습니다! 강사님이 설명을 너무 잘 해주셔서 처음 보는 내용도 쏙쏙 이해됐어요.</p>"
                       + "<p>특강에서 나온 주요 내용 정리 + 실습 코드 GitHub에 올려놨으니 참고하세요!</p>"
                       + "<ul><li>EC2 + RDS 기본 세팅</li><li>S3 버킷 퍼블릭 설정 방법</li><li>CloudFront 배포</li></ul>")
                .author(choi).category("캠퍼스").campus("구미")
                .viewCount(41).likeCount(11).build());

        postRepository.save(Post.builder()
                .title("[부울경] AI 특화 과정 시작! 14기 자기소개")
                .content("<h2>부울경 14기 AI 특화 과정 시작합니다 🌊</h2>"
                       + "<p>안녕하세요! 부울경 14기 AI 특화 과정이 드디어 시작됐네요.</p>"
                       + "<p>저는 Python과 PyTorch를 주로 다루고 있는데, 이번 과정에서 MLOps까지 제대로 배워보고 싶습니다!</p>"
                       + "<p>부울경 14기 분들 모두 반갑습니다. 함께 열심히 해봐요! 💪</p>"
                       + "<img src=\"https://picsum.photos/seed/busan14/800/350\" style=\"max-width:100%;border-radius:8px;margin:12px 0\">")
                .author(jung).category("캠퍼스").campus("부울경")
                .viewCount(29).likeCount(7).build());

        // 알림: 멘토링 관련 알림 추가
        notificationRepository.save(Notification.builder()
                .receiver(lee)
                .message("🎓 김개발님이 멘토링을 신청했습니다.")
                .link("/mentoring/my").isRead(false).build());
        notificationRepository.save(Notification.builder()
                .receiver(admin)
                .message("🎓 정AI님이 멘토링을 신청했습니다.")
                .link("/mentoring/my").isRead(false).build());
        notificationRepository.save(Notification.builder()
                .receiver(park)
                .message("🎓 이싸피님이 멘토링을 신청했습니다.")
                .link("/mentoring/my").isRead(true).build());
        notificationRepository.save(Notification.builder()
                .receiver(choi)
                .message("😢 이싸피 멘토님이 멘토링 신청을 검토했지만 아쉽게도 수락하지 못했습니다.")
                .link("/mentors").isRead(false).build());

        // ──────────── 친구 관계 (Friendship 더미 데이터) ────────────
        friendshipRepository.save(Friendship.builder()
                .requester(kim).receiver(park).status("ACCEPTED").build());
        friendshipRepository.save(Friendship.builder()
                .requester(lee).receiver(jung).status("ACCEPTED").build());
        friendshipRepository.save(Friendship.builder()
                .requester(kim).receiver(choi).status("PENDING").build());
    }

    /** 채팅방 더미 데이터 (없는 경우만 생성) */
    private void initChatRooms() {
        if (chatRoomRepository.count() > 0) return;
        Member admin = memberRepository.findByUsername("admin").orElse(null);
        if (admin == null) return;
        List.of(
            new String[]{"SSAFY 일반", "SSAFY 관련 자유 주제 대화방입니다."},
            new String[]{"알고리즘 스터디", "알고리즘 문제 풀이 및 코딩 테스트 연습 대화방입니다."},
            new String[]{"Spring Boot 관련", "Spring Boot, JPA, 보안 등 백엔드 기술 토론방입니다."},
            new String[]{"React 프론트엔드", "React, Vue, 프론트엔드 기술 공유방입니다."},
            new String[]{"취준 정보 공유", "취준 활동, 면접 후기, 이력서 큰전 대화방입니다."}
        ).forEach(arr -> chatRoomRepository.save(
            ChatRoom.builder().name(arr[0]).description(arr[1]).creator(admin).build()
        ));
    }

    /** dldnwls001 ~ dldnwls200 테스트 계정 (서버 재시작 시 없는 계정만 생성) */
    private void initTestAccounts() {
        String[] campuses = {"서울", "대전", "광주", "구미", "부울경"};

        for (int i = 1; i <= 200; i++) {
            String username = String.format("dldnwls%03d", i);
            if (memberRepository.findByUsername(username).isPresent()) continue;

            String campus  = campuses[(i - 1) % campuses.length];
            int cohort     = 11 + ((i - 1) % 4);          // 11 ~ 14기
            int classCode  = ((i - 1) % 8) + 1;           // 1 ~ 8반
            memberRepository.save(Member.builder()
                    .username(username)
                    .password(passwordEncoder.encode("011!011lee"))
                    .nickname(username)
                    .email("dldnwls009@gmail.com")
                    .bio("테스트 계정 " + username)
                    .campus(campus)
                    .cohort(cohort)
                    .classCode(classCode)
                    .realName(username)
                    .role("USER")
                    .build());
        }
    }
}
