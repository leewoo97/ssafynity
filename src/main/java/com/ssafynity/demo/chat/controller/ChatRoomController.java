package com.ssafynity.demo.chat.controller;

import com.ssafynity.demo.chat.domain.ChatRoom;
import com.ssafynity.demo.chat.dto.ChatMessageDto;
import com.ssafynity.demo.chat.dto.response.ChatRoomResponse;
import com.ssafynity.demo.chat.service.ChatMessageService;
import com.ssafynity.demo.chat.service.ChatRoomService;
import com.ssafynity.demo.common.exception.BusinessException;
import com.ssafynity.demo.common.exception.ErrorCode;
import com.ssafynity.demo.common.response.ApiResponse;
import com.ssafynity.demo.domain.Member;
import com.ssafynity.demo.security.CustomUserDetails;
import com.ssafynity.demo.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat/rooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final MemberService memberService;
    private final ChatMessageService chatMessageService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ChatRoomResponse>>> list() {
        List<ChatRoomResponse> result = chatRoomService.findAll().stream()
                .map(ChatRoomResponse::from).toList();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> detail(@PathVariable Long id) {
        ChatRoom room = chatRoomService.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND));
        return ResponseEntity.ok(ApiResponse.ok(ChatRoomResponse.from(room)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> create(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Member member = memberService.getById(userDetails.getId());
        ChatRoom room = chatRoomService.create(
                body.get("name"),
                body.getOrDefault("description", ""),
                member);
        return ResponseEntity.ok(ApiResponse.ok(ChatRoomResponse.from(room)));
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<ApiResponse<List<ChatMessageDto>>> messages(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(chatMessageService.getRecentMessages(id)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        chatRoomService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
