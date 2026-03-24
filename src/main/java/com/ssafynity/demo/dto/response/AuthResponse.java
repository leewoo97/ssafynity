package com.ssafynity.demo.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * 로그인 성공 응답.
 * accessToken 을 클라이언트가 로컬 스토리지 또는 메모리에 저장하고
 * 이후 요청마다 Authorization: Bearer {token} 헤더로 전달한다.
 */
@Getter
@Builder
public class AuthResponse {
    private String accessToken;
    private String tokenType;     // "Bearer"
    private MemberResponse member;
}
