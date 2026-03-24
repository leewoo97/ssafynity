package com.ssafynity.demo.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

/**
 * JWT 토큰 생성 / 검증 / 파싱 유틸리티.
 * <p>
 * - 서명 알고리즘: HS256
 * - 토큰 유효 기간: application.properties jwt.expiration-ms 설정값 (기본 24시간)
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long expirationMs;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms}") long expirationMs) {
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = expirationMs;
    }

    /** 인증 성공 후 JWT 토큰 생성 */
    public String generateToken(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return generateToken(userDetails.getUsername(), userDetails.getId());
    }

    /** username + memberId 로 JWT 생성 (테스트 / 재발급 용도) */
    public String generateToken(String username, Long memberId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(username)
                .claim("memberId", memberId)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    /** 토큰에서 username 추출 */
    public String getUsernameFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    /** 토큰에서 memberId 추출 */
    public Long getMemberIdFromToken(String token) {
        return parseClaims(token).get("memberId", Long.class);
    }

    /** 토큰 유효성 검증 */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("[JWT] 토큰 만료: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("[JWT] 지원되지 않는 토큰: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("[JWT] 손상된 토큰: {}", e.getMessage());
        } catch (SecurityException e) {
            log.warn("[JWT] 서명 불일치: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("[JWT] 빈 토큰: {}", e.getMessage());
        }
        return false;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
