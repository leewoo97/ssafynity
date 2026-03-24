package com.ssafynity.demo.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 도메인별 에러 코드 정의.
 * 각 코드는 HTTP 상태와 기본 메시지를 포함한다.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // ── 공통 ──────────────────────────────────────────────────────────────────
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),

    // ── 인증 / 인가 ──────────────────────────────────────────────────────────
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 없습니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),

    // ── 회원 ─────────────────────────────────────────────────────────────────
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."),
    DUPLICATE_USERNAME(HttpStatus.CONFLICT, "이미 사용 중인 아이디입니다."),
    WRONG_PASSWORD(HttpStatus.BAD_REQUEST, "현재 비밀번호가 올바르지 않습니다."),

    // ── 게시글 ────────────────────────────────────────────────────────────────
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 게시글입니다."),
    POST_ACCESS_DENIED(HttpStatus.FORBIDDEN, "게시글 수정/삭제 권한이 없습니다."),

    // ── 댓글 ─────────────────────────────────────────────────────────────────
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 댓글입니다."),
    COMMENT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "댓글 삭제 권한이 없습니다."),

    // ── 이벤트 ────────────────────────────────────────────────────────────────
    EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 이벤트입니다."),
    EVENT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "이벤트 수정/삭제 권한이 없습니다."),
    EVENT_FULL(HttpStatus.BAD_REQUEST, "이벤트 참가 정원이 꽉 찼습니다."),

    // ── 프로젝트 ──────────────────────────────────────────────────────────────
    PROJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 프로젝트입니다."),
    PROJECT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "프로젝트 수정/삭제 권한이 없습니다."),

    // ── 기술 문서 ─────────────────────────────────────────────────────────────
    TECH_DOC_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 기술 문서입니다."),
    TECH_DOC_ACCESS_DENIED(HttpStatus.FORBIDDEN, "기술 문서 수정/삭제 권한이 없습니다."),

    // ── 영상 ─────────────────────────────────────────────────────────────────
    TECH_VIDEO_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 영상입니다."),
    TECH_VIDEO_ACCESS_DENIED(HttpStatus.FORBIDDEN, "영상 수정/삭제 권한이 없습니다."),

    // ── 채팅방 ────────────────────────────────────────────────────────────────
    CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 채팅방입니다."),

    // ── DM ────────────────────────────────────────────────────────────────────
    DM_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 대화방입니다."),
    DM_ACCESS_DENIED(HttpStatus.FORBIDDEN, "대화방에 대한 권한이 없습니다."),
    DM_NOT_FRIEND(HttpStatus.FORBIDDEN, "친구 관계인 회원과만 대화를 시작할 수 있습니다."),

    // ── 친구 ─────────────────────────────────────────────────────────────────
    FRIENDSHIP_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 친구 관계입니다."),
    FRIENDSHIP_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 친구 요청을 보냈거나 친구 관계입니다."),

    // ── 멘토링 ────────────────────────────────────────────────────────────────
    MENTOR_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 멘토입니다."),
    MENTOR_ALREADY_REGISTERED(HttpStatus.CONFLICT, "이미 멘토로 등록되어 있습니다."),
    MENTORING_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 멘토링 요청입니다."),
    MENTORING_ALREADY_APPLIED(HttpStatus.CONFLICT, "이미 멘토링 요청을 보냈습니다."),

    // ── 알림 ─────────────────────────────────────────────────────────────────
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 알림입니다."),

    // ── 신고 ─────────────────────────────────────────────────────────────────
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 신고입니다.");

    private final HttpStatus httpStatus;
    private final String defaultMessage;
}
