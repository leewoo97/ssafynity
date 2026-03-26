# SSAFYnity

SSAFY 수료생을 위한 커뮤니티 플랫폼. 게시판, 실시간 채팅(공개방 / DM / 그룹), 멘토링, 기술 문서 공유 등의 기능을 제공합니다.

---

## 기술 스택

### Backend

| 구분 | 기술 |
|---|---|
| 프레임워크 | Spring Boot 4.0.3 (Spring 7) |
| 언어 | Java 21 |
| 보안 | Spring Security 7 + JWT (jjwt 0.12.6) |
| 데이터베이스 | H2 (In-Memory, 개발용) |
| ORM | Spring Data JPA + Hibernate |
| 동적 쿼리 | QueryDSL 5.1.0 |
| 실시간 통신 | Spring WebSocket + STOMP |
| 메시지 브로커 | Redis Pub/Sub |
| 빌드 | Gradle 9 |
| 이메일 | Spring Mail (Gmail SMTP) |
| 파일 업로드 | Spring Multipart (로컬 `/uploads/`) |

### Frontend

| 구분 | 기술 |
|---|---|
| 프레임워크 | React 18 |
| 빌드 도구 | Vite 5 |
| 라우팅 | React Router DOM 6 |
| 상태 관리 | Zustand 5 |
| HTTP | Axios |
| WebSocket | @stomp/stompjs 7 + SockJS |
| 에디터 | Quill (react-quill) |
| 날짜 | dayjs |

---

## 아키텍처 개요

```
 Browser (React SPA)
     │
     │  HTTP (REST API)          WebSocket (STOMP over SockJS)
     │  /api/**                  /ws
     ▼                           ▼
 ┌───────────────────────────────────────────────────┐
 │                  Spring Boot                       │
 │                                                    │
 │  JwtAuthenticationFilter ──► SecurityFilterChain  │
 │                                                    │
 │  REST Controllers          WebSocket Controllers   │
 │  ─────────────────         ───────────────────     │
 │  AuthController            ChatController          │
 │  PostController            DmChatController        │
 │  DmController              (STOMP @MessageMapping) │
 │  MemberController                                  │
 │  ...                                               │
 │                    │                               │
 │              Service Layer                         │
 │                    │                               │
 │           JPA Repository Layer                     │
 │                    │                               │
 │              H2 Database                           │
 │                                                    │
 │  RedisPublisher  ──────────►  Redis Pub/Sub        │
 │                                    │               │
 │  RedisSubscriber ◄─────────────────┘               │
 │       │                                            │
 │       ▼                                            │
 │  STOMP Broker (/topic/chat/{id}, /topic/dm/{id})  │
 └───────────────────────────────────────────────────┘
```

---

## 프로젝트 구조

```
demo/
├── src/main/java/com/ssafynity/demo/
│   ├── config/
│   │   ├── SecurityConfig.java        # Spring Security 설정 (JWT, CORS, 인가)
│   │   ├── WebSocketConfig.java       # STOMP 엔드포인트, JWT 인증 인터셉터
│   │   ├── RedisConfig.java           # Redis 템플릿, Pub/Sub 리스너 컨테이너
│   │   ├── QuerydslConfig.java        # QueryDSL JPAQueryFactory 빈
│   │   └── WebConfig.java             # 정적 파일 핸들러 (uploads/)
│   │
│   ├── security/
│   │   ├── JwtTokenProvider.java      # JWT 생성/검증 (HS256)
│   │   ├── JwtAuthenticationFilter.java # 요청마다 JWT 파싱 → SecurityContext 주입
│   │   ├── CustomUserDetails.java     # UserDetails 구현체 (id, nickname 포함)
│   │   └── CustomUserDetailsService.java
│   │
│   ├── chat/
│   │   ├── RedisPublisher.java        # ChatMessageDto → Redis topic 발행
│   │   └── RedisSubscriber.java       # Redis 메시지 수신 → STOMP 브로드캐스트
│   │
│   ├── controller/
│   │   ├── AuthController.java        # 회원가입, 로그인, 이메일 인증
│   │   ├── HomeController.java        # 홈 데이터, SPA 폴백
│   │   ├── MemberController.java      # 회원 조회, 수정, 비밀번호 변경
│   │   ├── ProfileController.java     # 프로필 이미지 업로드
│   │   ├── PostController.java        # 게시글 CRUD + 좋아요
│   │   ├── CommentController.java     # 댓글 CRUD
│   │   ├── EventController.java       # 이벤트/행사 CRUD
│   │   ├── ProjectController.java     # 프로젝트 모집 CRUD
│   │   ├── TechDocController.java     # 기술 문서 CRUD
│   │   ├── TechVideoController.java   # 기술 영상 CRUD
│   │   ├── BookmarkController.java    # 북마크 추가/삭제/목록
│   │   ├── SearchController.java      # 통합 검색
│   │   ├── NotificationController.java # 알림 목록/읽음 처리
│   │   ├── FriendController.java      # 친구 추가/삭제
│   │   ├── ChatRoomController.java    # 공개 채팅방 REST
│   │   ├── ChatController.java        # 공개 채팅 STOMP (/app/chat.*)
│   │   ├── DmController.java          # DM/그룹 채팅방 REST
│   │   ├── DmChatController.java      # DM 채팅 STOMP (/app/dm.*)
│   │   ├── MentoringController.java   # 멘토 등록/멘토링 신청
│   │   ├── ReportController.java      # 신고 접수
│   │   ├── UploadController.java      # 파일 업로드
│   │   ├── AdminController.java       # 관리자 대시보드
│   │   └── CampusController.java      # 캠퍼스 목록
│   │
│   ├── service/                       # 비즈니스 로직 (각 도메인별)
│   ├── repository/                    # JPA Repository + QueryDSL Impl
│   ├── domain/                        # JPA 엔티티
│   ├── dto/request/                   # 요청 DTO
│   ├── dto/response/                  # 응답 DTO
│   ├── common/                        # ApiResponse, ErrorCode, GlobalExceptionHandler
│   └── init/DataInitializer.java      # 개발용 초기 데이터 (CommandLineRunner)
│
├── src/main/resources/
│   ├── application.properties         # 공통 설정
│   └── application-local.properties   # 로컬 환경 (메일 계정 등, git 제외 권장)
│
└── frontend/                          # React SPA
    ├── src/
    │   ├── App.jsx                    # 라우터 정의
    │   ├── main.jsx
    │   ├── api/axios.js               # Axios 인스턴스 + JWT 인터셉터 + 401 처리
    │   ├── store/authStore.js         # Zustand - 토큰/회원 정보 영속
    │   ├── components/
    │   │   ├── Layout.jsx             # 네비게이션 바 + ChatPanel 슬라이딩 패널
    │   │   ├── ChatPanel.jsx          # 우측 슬라이딩 채팅 패널 (DM 목록 + 인라인 채팅)
    │   │   └── RichEditor.jsx         # Quill 에디터 래퍼
    │   └── pages/                     # 각 기능 페이지 (아래 목록 참고)
    └── vite.config.js                 # 개발 서버 프록시 (/api, /ws → localhost:8080)
```

---

## 도메인 모델

```
Member ──┬── Post (작성자)
         ├── Comment (작성자)
         ├── PostLike
         ├── Bookmark
         ├── Notification
         ├── Friendship (친구 관계, 양방향)
         ├── MentorProfile (1:1)
         ├── MentoringRequest (신청자/멘토)
         ├── Report
         ├── DirectRoomMember ──► DirectRoom (DM/그룹 채팅방)
         │                              └── DirectMessage
         └── ChatRoom (공개 채팅방)
                 └── ChatMessage
```

---

## 실시간 채팅 흐름

### 공개 채팅방
```
Client  →  STOMP /app/chat.message  →  ChatController
                                            │ DB 저장 + Redis publish ("chat")
                                     RedisSubscriber
                                            │
                                     STOMP /topic/chat/{roomId}  →  구독 클라이언트 전체
```

### DM / 그룹 채팅
```
Client  →  STOMP /app/dm.send  →  DmChatController
                                       │ 참여자 확인 + DB 저장 + Redis publish ("chat", channel=DM)
                                  RedisSubscriber
                                       │
                                  STOMP /topic/dm/{roomId}  →  구독 클라이언트 전체
```

### WebSocket 인증
```
STOMP CONNECT 프레임
  Authorization: Bearer {JWT}
        │
  WebSocketConfig (ChannelInterceptor)
        │ JWT 검증 → accessor.setUser(UsernamePasswordAuthenticationToken)
        │
  ChatController / DmChatController
        │ headerAccessor.getUser() → CustomUserDetails → Member ID → DB 조회
```

---

## 주요 API 엔드포인트

### 인증 `/api/auth`
| Method | Path | 설명 |
|---|---|---|
| POST | `/api/auth/register` | 회원가입 |
| POST | `/api/auth/login` | 로그인 (JWT 반환) |
| POST | `/api/auth/send-verification` | 이메일 인증 코드 발송 |
| POST | `/api/auth/verify-email` | 이메일 인증 코드 확인 |

### 회원 `/api/members`
| Method | Path | 설명 |
|---|---|---|
| GET | `/api/members/me` | 내 정보 |
| GET | `/api/members/search?q=` | 회원 검색 (q 생략 시 전체) |
| PUT | `/api/members/me` | 프로필 수정 |
| PUT | `/api/members/me/password` | 비밀번호 변경 |

### 게시글 `/api/posts`
| Method | Path | 설명 |
|---|---|---|
| GET | `/api/posts` | 목록 (페이징, 필터) |
| POST | `/api/posts` | 작성 |
| GET | `/api/posts/{id}` | 상세 |
| PUT | `/api/posts/{id}` | 수정 |
| DELETE | `/api/posts/{id}` | 삭제 |
| POST | `/api/posts/{id}/like` | 좋아요 토글 |

### DM `/api/dm`
| Method | Path | 설명 |
|---|---|---|
| GET | `/api/dm/rooms` | 내 채팅방 목록 |
| POST | `/api/dm/users/{targetId}` | 1:1 DM 시작 (없으면 생성) |
| POST | `/api/dm/group` | 그룹 채팅방 생성 |
| GET | `/api/dm/rooms/{roomId}/messages` | 메시지 목록 |

### WebSocket STOMP
| Destination | 방향 | 설명 |
|---|---|---|
| `/app/chat.message` | 발행 | 공개 채팅 메시지 전송 |
| `/app/chat.join` | 발행 | 채팅방 입장 |
| `/app/dm.send` | 발행 | DM/그룹 메시지 전송 |
| `/app/dm.join` | 발행 | DM방 입장 알림 |
| `/topic/chat/{roomId}` | 구독 | 공개 채팅 수신 |
| `/topic/dm/{roomId}` | 구독 | DM/그룹 채팅 수신 |

---

## 프론트엔드 페이지 목록

| 경로 | 페이지 | 설명 |
|---|---|---|
| `/` | HomePage | 홈 (통계, 인기글, 최신 문서) |
| `/login` | LoginPage | 로그인 |
| `/register` | RegisterPage | 회원가입 + 이메일 인증 |
| `/mypage` | MyPage | 내 정보 수정 |
| `/profile/:id` | ProfilePage | 타 회원 프로필 |
| `/posts` | PostListPage | 게시글 목록 |
| `/posts/:id` | PostDetailPage | 게시글 상세 + 댓글 |
| `/posts/new` | PostFormPage | 게시글 작성/수정 |
| `/events` | EventListPage | 이벤트/행사 목록 |
| `/projects` | ProjectListPage | 프로젝트 모집 목록 |
| `/docs` | DocListPage | 기술 문서 목록 |
| `/videos` | VideoListPage | 기술 영상 목록 |
| `/search` | SearchPage | 통합 검색 결과 |
| `/notifications` | NotificationsPage | 알림 목록 |
| `/chat` | ChatRoomsPage | 공개 채팅방 목록 |
| `/chat/:id` | ChatRoomPage | 공개 채팅룸 |
| `/dm` | DmListPage | DM 목록 + 그룹 생성 |
| `/dm/:id` | DmRoomPage | DM/그룹 채팅룸 |
| `/mentors` | MentorListPage | 멘토 목록 |
| `/mentors/register` | MentorRegisterPage | 멘토 등록 |
| `/mentoring` | MentoringPage | 멘토링 신청/현황 |
| `/admin` | AdminPage | 관리자 대시보드 |

> **ChatPanel**: 모든 페이지에서 우측 슬라이딩 패널로 접근 가능한 DM 채팅 패널 (이동 없이 인라인 채팅)

---

## 로컬 실행 방법

### 사전 요구사항
- Java 21
- Node.js 20+
- Redis 7 (`docker run -d -p 6379:6379 redis:7-alpine`)

### 백엔드
```bash
# 프로젝트 루트에서
./gradlew bootRun
# → http://localhost:8080
```

### 프론트엔드
```bash
cd frontend
npm install
npm run dev
# → http://localhost:5173
```

### H2 콘솔
```
http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:testdb
```

---

## 환경 변수 / 설정

`src/main/resources/application.properties`

| 키 | 기본값 | 설명 |
|---|---|---|
| `jwt.secret` | (base64 인코딩된 256bit 키) | JWT 서명 키 |
| `jwt.expiration-ms` | `86400000` (24시간) | JWT 만료 시간 |
| `cors.allowed-origins` | `http://localhost:5173` | CORS 허용 출처 |
| `spring.data.redis.host` | `localhost` | Redis 호스트 |
| `spring.data.redis.port` | `6379` | Redis 포트 |
| `app.upload.dir` | `uploads` | 파일 업로드 경로 |
| `spring.mail.username` | - | Gmail 계정 |
| `spring.mail.password` | - | Gmail 앱 비밀번호 |

> 메일 미설정 시 이메일 발송은 로그로만 출력되며 서버는 정상 동작합니다.

---

## ChatPanel 인라인 채팅 흐름

네비게이션의 **💬 팀 채팅** 버튼 → 우측에서 슬라이딩 패널 등장

```
ChatPanel (open state)
  ├── 방 목록 뷰 (기본)
  │     ├── 검색 바
  │     ├── 다이렉트 / 멘션 탭
  │     ├── RoomItem 목록 (실시간 unread badge)
  │     └── "+ 새 대화 시작" 버튼
  │
  ├── startMode = 'pick'   → 1:1 대화 / 그룹 대화 선택
  ├── startMode = 'dm'     → 멤버 단일 선택 → POST /api/dm/users/{id}
  ├── startMode = 'group1' → 멤버 다중 선택
  ├── startMode = 'group2' → 그룹 이름 입력 → POST /api/dm/group
  │
  └── activeRoom           → InlineRoom (WebSocket 연결 + 메시지 송수신)
```
