package com.ssafynity.demo.controller;

import com.ssafynity.demo.common.response.ApiResponse;
import com.ssafynity.demo.domain.Member;
import com.ssafynity.demo.dto.response.BookmarkResponse;
import com.ssafynity.demo.security.CustomUserDetails;
import com.ssafynity.demo.service.BookmarkService;
import com.ssafynity.demo.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookmarks")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class BookmarkController {

    private final BookmarkService bookmarkService;
    private final MemberService memberService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<BookmarkResponse>>> list(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Member me = memberService.getById(userDetails.getId());
        List<BookmarkResponse> result = bookmarkService.findByMember(me).stream()
                .map(BookmarkResponse::from).toList();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PostMapping("/toggle")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> toggle(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Member me = memberService.getById(userDetails.getId());
        String targetType = (String) body.get("targetType");
        Long targetId = ((Number) body.get("targetId")).longValue();
        String targetTitle = (String) body.getOrDefault("targetTitle", "");
        boolean bookmarked = bookmarkService.toggle(me, targetType, targetId, targetTitle);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("bookmarked", bookmarked)));
    }

    @GetMapping("/check")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> check(
            @RequestParam String targetType,
            @RequestParam Long targetId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Member me = memberService.getById(userDetails.getId());
        boolean bookmarked = bookmarkService.isBookmarked(me, targetType, targetId);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("bookmarked", bookmarked)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        bookmarkService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
