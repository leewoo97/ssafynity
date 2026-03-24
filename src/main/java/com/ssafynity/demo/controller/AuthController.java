package com.ssafynity.demo.controller;

import com.ssafynity.demo.common.exception.BusinessException;
import com.ssafynity.demo.common.exception.ErrorCode;
import com.ssafynity.demo.common.response.ApiResponse;
import com.ssafynity.demo.domain.Member;
import com.ssafynity.demo.dto.request.LoginRequest;
import com.ssafynity.demo.dto.request.RegisterRequest;
import com.ssafynity.demo.dto.response.AuthResponse;
import com.ssafynity.demo.dto.response.MemberResponse;
import com.ssafynity.demo.security.JwtTokenProvider;
import com.ssafynity.demo.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 인증/인가 API
 * POST /api/auth/register  → 회원가입
 * POST /api/auth/login     → 로그인 (JWT 발급)
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberService memberService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<MemberResponse>> register(@Valid @RequestBody RegisterRequest req) {
        Member member = memberService.register(
                req.getUsername(), req.getPassword(), req.getNickname(),
                req.getEmail(), req.getRealName(), req.getCampus(),
                req.getCohort(), req.getClassCode());
        return ResponseEntity.ok(ApiResponse.ok(MemberResponse.of(member)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest req) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));

            String token = jwtTokenProvider.generateToken(authentication);
            Member member = memberService.findByUsername(req.getUsername())
                    .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

            AuthResponse authResponse = AuthResponse.builder()
                    .accessToken(token)
                    .tokenType("Bearer")
                    .member(MemberResponse.of(member))
                    .build();

            return ResponseEntity.ok(ApiResponse.ok(authResponse));
        } catch (BadCredentialsException e) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
    }
}
