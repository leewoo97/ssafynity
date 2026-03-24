package com.ssafynity.demo.controller;

import com.ssafynity.demo.common.response.ApiResponse;
import com.ssafynity.demo.domain.Member;
import com.ssafynity.demo.dto.response.NotificationResponse;
import com.ssafynity.demo.security.CustomUserDetails;
import com.ssafynity.demo.service.MemberService;
import com.ssafynity.demo.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class NotificationController {

    private final NotificationService notificationService;
    private final MemberService memberService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> list(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Member me = memberService.getById(userDetails.getId());
        List<NotificationResponse> result = notificationService.findByReceiver(me).stream()
                .map(NotificationResponse::from).toList();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> unreadCount(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Member me = memberService.getById(userDetails.getId());
        int count = notificationService.countUnread(me);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("count", count)));
    }

    @PostMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllRead(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Member me = memberService.getById(userDetails.getId());
        notificationService.markAllRead(me);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
