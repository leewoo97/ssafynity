package com.ssafynity.demo.controller;

import com.ssafynity.demo.common.response.ApiResponse;
import com.ssafynity.demo.chat.domain.ChatRoom;
import com.ssafynity.demo.domain.Member;
import com.ssafynity.demo.dto.response.MemberResponse;
import com.ssafynity.demo.dto.response.ReportResponse;
import com.ssafynity.demo.service.BookmarkService;
import com.ssafynity.demo.chat.service.ChatRoomService;
import com.ssafynity.demo.service.CommentService;
import com.ssafynity.demo.service.MemberService;
import com.ssafynity.demo.service.PostService;
import com.ssafynity.demo.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final MemberService memberService;
    private final PostService postService;
    private final CommentService commentService;
    private final ReportService reportService;
    private final ChatRoomService chatRoomService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> dashboard() {
        Map<String, Object> stats = Map.of(
                "totalMembers", memberService.findAll().size(),
                "totalPosts", postService.findAll().size(),
                "totalComments", commentService.countAll(),
                "pendingReports", reportService.findPending().size()
        );
        return ResponseEntity.ok(ApiResponse.ok(stats));
    }

    @GetMapping("/members")
    public ResponseEntity<ApiResponse<List<MemberResponse>>> members() {
        List<MemberResponse> result = memberService.findAll().stream()
                .map(MemberResponse::ofWithRealName).toList();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @DeleteMapping("/members/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMember(@PathVariable Long id) {
        memberService.deleteMember(id);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @GetMapping("/reports")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<ReportResponse>>> reports() {
        List<ReportResponse> result = reportService.findPending().stream()
                .map(ReportResponse::from).toList();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PostMapping("/reports/{id}/resolve")
    public ResponseEntity<ApiResponse<Void>> resolveReport(@PathVariable Long id) {
        reportService.resolve(id);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    /** 채팅방 실시간 접속자 모니터 — Redis Set 기반 카운트 + 닉네임 목록 */
    @GetMapping("/chat/rooms")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> chatRooms() {
        // Redis에서 현재 접속 중인 roomId → userId Set 직접 스캔
        Map<String, java.util.Set<String>> activeMap = chatRoomService.getAllActiveRoomUsers();

        // DB 채팅방 목록 (이름/설명 용도)
        Map<Long, ChatRoom> roomIndex = chatRoomService.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(
                        ChatRoom::getId,
                        r -> r));

        // Redis에 있는 roomId 기준으로 머지
        java.util.Set<Long> allRoomIds = new java.util.LinkedHashSet<>();
        roomIndex.keySet().forEach(allRoomIds::add);
        activeMap.keySet().forEach(k -> allRoomIds.add(Long.parseLong(k)));

        List<Map<String, Object>> result = allRoomIds.stream().map(roomId -> {
            Map<String, Object> m = new LinkedHashMap<>();
            ChatRoom room = roomIndex.get(roomId);
            m.put("id", roomId);
            m.put("name", room != null ? room.getName() : "방 #" + roomId);
            m.put("description", room != null && room.getDescription() != null ? room.getDescription() : "");
            java.util.Set<String> userIdStrs = activeMap.getOrDefault(String.valueOf(roomId), java.util.Collections.emptySet());
            m.put("activeUsers", userIdStrs.size());
            m.put("activeUserNicknames", chatRoomService.getNicknamesByIds(userIdStrs));
            return m;
        }).toList();

        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
