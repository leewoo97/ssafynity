package com.ssafynity.demo.controller;

import com.ssafynity.demo.common.response.ApiResponse;
import com.ssafynity.demo.domain.Member;
import com.ssafynity.demo.domain.MentorProfile;
import com.ssafynity.demo.domain.MentoringRequest;
import com.ssafynity.demo.dto.response.MentorProfileResponse;
import com.ssafynity.demo.dto.response.MentoringRequestResponse;
import com.ssafynity.demo.security.CustomUserDetails;
import com.ssafynity.demo.service.MemberService;
import com.ssafynity.demo.service.MentoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class MentoringController {

    private final MentoringService mentoringService;
    private final MemberService memberService;

    // ────────────── 멘토 프로필 ──────────────

    @GetMapping("/api/mentors")
    public ResponseEntity<ApiResponse<List<MentorProfileResponse>>> list(
            @RequestParam(required = false) String keyword) {
        List<MentorProfile> mentors = (keyword != null)
                ? mentoringService.searchBySpecialty(keyword)
                : mentoringService.findAllActive();
        List<MentorProfileResponse> result = mentors.stream()
                .map(MentorProfileResponse::from).toList();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/api/mentors/{id}")
    public ResponseEntity<ApiResponse<MentorProfileResponse>> detail(@PathVariable Long id) {
        MentorProfile profile = mentoringService.findById(id)
                .orElseThrow(() -> new com.ssafynity.demo.common.exception.BusinessException(
                        com.ssafynity.demo.common.exception.ErrorCode.MENTOR_NOT_FOUND));
        return ResponseEntity.ok(ApiResponse.ok(MentorProfileResponse.from(profile)));
    }

    @PostMapping("/api/mentors")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<MentorProfileResponse>> register(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Member member = memberService.getById(userDetails.getId());
        int maxMentees = body.containsKey("maxMentees") ? ((Number) body.get("maxMentees")).intValue() : 3;
        MentorProfile profile = mentoringService.registerMentor(
                member,
                (String) body.get("title"),
                (String) body.get("career"),
                (String) body.get("specialty"),
                (String) body.get("introduction"),
                maxMentees
        );
        return ResponseEntity.ok(ApiResponse.ok(MentorProfileResponse.from(profile)));
    }

    @PutMapping("/api/mentors/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> update(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Member member = memberService.getById(userDetails.getId());
        MentorProfile profile = mentoringService.findByMember(member)
                .orElseThrow(() -> new com.ssafynity.demo.common.exception.BusinessException(
                        com.ssafynity.demo.common.exception.ErrorCode.MENTOR_NOT_FOUND));
        int maxMentees = body.containsKey("maxMentees") ? ((Number) body.get("maxMentees")).intValue() : 3;
        mentoringService.updateMentor(
                profile.getId(),
                (String) body.get("title"),
                (String) body.get("career"),
                (String) body.get("specialty"),
                (String) body.get("introduction"),
                maxMentees
        );
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @PostMapping("/api/mentors/me/toggle")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> toggle(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Member member = memberService.getById(userDetails.getId());
        MentorProfile profile = mentoringService.findByMember(member)
                .orElseThrow(() -> new com.ssafynity.demo.common.exception.BusinessException(
                        com.ssafynity.demo.common.exception.ErrorCode.MENTOR_NOT_FOUND));
        mentoringService.toggleActive(profile.getId());
        return ResponseEntity.ok(ApiResponse.ok());
    }

    // ────────────── 멘토링 신청 ──────────────

    @PostMapping("/api/mentoring/apply/{mentorProfileId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<MentoringRequestResponse>> apply(
            @PathVariable Long mentorProfileId,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Member mentee = memberService.getById(userDetails.getId());
        MentoringRequest req = mentoringService.applyForMentoring(
                mentee, mentorProfileId, body.getOrDefault("message", ""));
        return ResponseEntity.ok(ApiResponse.ok(MentoringRequestResponse.from(req)));
    }

    @GetMapping("/api/mentoring/requests/received")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<MentoringRequestResponse>>> received(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Member member = memberService.getById(userDetails.getId());
        MentorProfile profile = mentoringService.findByMember(member)
                .orElseThrow(() -> new com.ssafynity.demo.common.exception.BusinessException(
                        com.ssafynity.demo.common.exception.ErrorCode.MENTOR_NOT_FOUND));
        List<MentoringRequestResponse> result = mentoringService.findRequestsByMentor(member).stream()
                .map(MentoringRequestResponse::from).toList();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/api/mentoring/requests/sent")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<MentoringRequestResponse>>> sent(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Member member = memberService.getById(userDetails.getId());
        List<MentoringRequestResponse> result = mentoringService.findRequestsByMentee(member).stream()
                .map(MentoringRequestResponse::from).toList();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PostMapping("/api/mentoring/requests/{requestId}/accept")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> accept(
            @PathVariable Long requestId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Member mentor = memberService.getById(userDetails.getId());
        mentoringService.acceptRequest(requestId, mentor);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @PostMapping("/api/mentoring/requests/{requestId}/reject")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> reject(
            @PathVariable Long requestId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Member mentor = memberService.getById(userDetails.getId());
        mentoringService.rejectRequest(requestId, mentor);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
