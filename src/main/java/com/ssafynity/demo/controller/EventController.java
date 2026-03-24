package com.ssafynity.demo.controller;

import com.ssafynity.demo.common.exception.BusinessException;
import com.ssafynity.demo.common.exception.ErrorCode;
import com.ssafynity.demo.common.response.ApiResponse;
import com.ssafynity.demo.domain.Event;
import com.ssafynity.demo.domain.Member;
import com.ssafynity.demo.dto.request.EventRequest;
import com.ssafynity.demo.dto.response.EventResponse;
import com.ssafynity.demo.security.CustomUserDetails;
import com.ssafynity.demo.service.EventService;
import com.ssafynity.demo.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 이벤트 REST API
 * GET    /api/events            → 전체 목록
 * GET    /api/events/{id}       → 상세 조회
 * POST   /api/events            → 생성 (로그인 필요)
 * PUT    /api/events/{id}       → 수정 (주최자만)
 * DELETE /api/events/{id}       → 삭제 (주최자 or ADMIN)
 * POST   /api/events/{id}/join  → 참가
 */
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final MemberService memberService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<EventResponse>>> list() {
        List<EventResponse> result = eventService.findAll().stream()
                .map(EventResponse::from).toList();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse<List<EventResponse>>> upcoming() {
        List<EventResponse> result = eventService.findUpcoming().stream()
                .map(EventResponse::from).toList();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventResponse>> detail(@PathVariable Long id) {
        Event event = eventService.getById(id);
        return ResponseEntity.ok(ApiResponse.ok(EventResponse.from(event)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<EventResponse>> create(
            @Valid @RequestBody EventRequest req,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Member member = memberService.getById(userDetails.getId());
        Event event = eventService.create(req, member);
        return ResponseEntity.ok(ApiResponse.ok(EventResponse.from(event)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> update(
            @PathVariable Long id,
            @Valid @RequestBody EventRequest req,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        eventService.update(id, req, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        eventService.delete(id, userDetails.getId(), userDetails.getRole());
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @PostMapping("/{id}/join")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> join(@PathVariable Long id) {
        eventService.join(id);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
