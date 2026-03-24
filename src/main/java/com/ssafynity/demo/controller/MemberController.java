package com.ssafynity.demo.controller;

import com.ssafynity.demo.common.response.ApiResponse;
import com.ssafynity.demo.domain.Member;
import com.ssafynity.demo.dto.request.ChangePasswordRequest;
import com.ssafynity.demo.dto.request.ProfileUpdateRequest;
import com.ssafynity.demo.dto.response.MemberResponse;
import com.ssafynity.demo.security.CustomUserDetails;
import com.ssafynity.demo.service.FriendshipService;
import com.ssafynity.demo.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 회원 REST API
 * GET    /api/members/me              → 내 정보
 * PUT    /api/members/me              → 내 정보 수정
 * POST   /api/members/me/password     → 비밀번호 변경
 * DELETE /api/members/me              → 탈퇴
 * GET    /api/members/{id}            → 특정 회원 조회
 * GET    /api/members                 → 전체 목록 (ADMIN)
 */
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final FriendshipService friendshipService;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<MemberResponse>> me(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Member member = memberService.getById(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.ok(MemberResponse.ofWithRealName(member)));
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<MemberResponse>> updateProfile(
            @Valid @RequestBody ProfileUpdateRequest req,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        memberService.updateProfile(userDetails.getId(), req);
        Member updated = memberService.getById(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.ok(MemberResponse.ofWithRealName(updated)));
    }

    @PostMapping("/me/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest req,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        memberService.changePassword(userDetails.getId(), req.getCurrentPassword(), req.getNewPassword());
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @DeleteMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> withdraw(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        memberService.deleteMember(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MemberResponse>> profile(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Member target = memberService.getById(id);
        if (userDetails != null) {
            Member viewer = memberService.getById(userDetails.getId());
            boolean canSeeReal = friendshipService.canSeeRealName(viewer, target);
            return ResponseEntity.ok(ApiResponse.ok(
                    canSeeReal ? MemberResponse.ofWithRealName(target) : MemberResponse.of(target)
            ));
        }
        return ResponseEntity.ok(ApiResponse.ok(MemberResponse.of(target)));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<MemberResponse>>> listAll() {
        List<MemberResponse> result = memberService.findAll().stream()
                .map(MemberResponse::ofWithRealName).toList();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
