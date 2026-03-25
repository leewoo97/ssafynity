package com.ssafynity.demo.controller;

import com.ssafynity.demo.common.response.ApiResponse;
import com.ssafynity.demo.domain.DirectMessage;
import com.ssafynity.demo.domain.DirectRoom;
import com.ssafynity.demo.domain.Member;
import com.ssafynity.demo.dto.request.GroupChatRequest;
import com.ssafynity.demo.dto.response.DirectMessageResponse;
import com.ssafynity.demo.dto.response.DirectRoomResponse;
import com.ssafynity.demo.dto.response.MemberResponse;
import com.ssafynity.demo.security.CustomUserDetails;
import com.ssafynity.demo.service.DirectMessageService;
import com.ssafynity.demo.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dm")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class DmController {

    private final DirectMessageService directMessageService;
    private final MemberService memberService;

    /** 내 DM 방 목록 */
    @GetMapping("/rooms")
    public ResponseEntity<ApiResponse<List<DirectRoomResponse>>> myRooms(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Member me = memberService.getById(userDetails.getId());
        List<DirectRoom> rooms = directMessageService.getRoomsForMember(me);
        List<DirectRoomResponse> result = rooms.stream()
                .map((DirectRoom room) -> {
                    Member other = "GROUP".equals(room.getType()) ? null : directMessageService.getOtherMember(room, me);
                    List<Member> members = "GROUP".equals(room.getType()) ? directMessageService.getMemberList(room) : null;
                    DirectMessage last = directMessageService.getLastMessage(room).orElse(null);
                    return DirectRoomResponse.from(room, other, members, last);
                }).toList();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    /** 1:1 DM 시작 (또는 기존 방 반환) */
    @PostMapping("/users/{targetId}")
    public ResponseEntity<ApiResponse<DirectRoomResponse>> startDm(
            @PathVariable Long targetId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Member me = memberService.getById(userDetails.getId());
        Member target = memberService.getById(targetId);
        DirectRoom room = directMessageService.findOrCreateDm(me, target);
        return ResponseEntity.ok(ApiResponse.ok(
                DirectRoomResponse.from(room, target, null, null)));
    }

    /** 그룹 DM 방 생성 */
    @PostMapping("/group")
    public ResponseEntity<ApiResponse<DirectRoomResponse>> createGroup(
            @Valid @RequestBody GroupChatRequest req,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Member me = memberService.getById(userDetails.getId());
        DirectRoom room = directMessageService.createGroup(me, req.getName(), req.getMemberIds());
        List<Member> members = directMessageService.getMemberList(room);
        return ResponseEntity.ok(ApiResponse.ok(
                DirectRoomResponse.from(room, null, members, null)));
    }

    /** 단일 방 조회 */
    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<ApiResponse<DirectRoomResponse>> getRoom(
            @PathVariable Long roomId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Member me = memberService.getById(userDetails.getId());
        DirectRoom room = directMessageService.findById(roomId)
                .orElseThrow(() -> new com.ssafynity.demo.common.exception.BusinessException(
                        com.ssafynity.demo.common.exception.ErrorCode.DM_ROOM_NOT_FOUND));
        if (!directMessageService.isMember(room, me)) {
            throw new com.ssafynity.demo.common.exception.BusinessException(
                    com.ssafynity.demo.common.exception.ErrorCode.DM_ACCESS_DENIED);
        }
        Member other = "GROUP".equals(room.getType()) ? null : directMessageService.getOtherMember(room, me);
        List<Member> members = "GROUP".equals(room.getType()) ? directMessageService.getMemberList(room) : null;
        DirectMessage last = directMessageService.getLastMessage(room).orElse(null);
        return ResponseEntity.ok(ApiResponse.ok(DirectRoomResponse.from(room, other, members, last)));
    }

    /** 방의 메시지 목록 */
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<ApiResponse<List<DirectMessageResponse>>> messages(
            @PathVariable Long roomId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Member me = memberService.getById(userDetails.getId());
        DirectRoom room = directMessageService.findById(roomId)
                .orElseThrow(() -> new com.ssafynity.demo.common.exception.BusinessException(
                        com.ssafynity.demo.common.exception.ErrorCode.DM_ROOM_NOT_FOUND));
        if (!directMessageService.isMember(room, me)) {
            throw new com.ssafynity.demo.common.exception.BusinessException(
                    com.ssafynity.demo.common.exception.ErrorCode.DM_ACCESS_DENIED);
        }
        List<DirectMessageResponse> result = directMessageService.getMessages(room).stream()
                .map(DirectMessageResponse::from).toList();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    /** 방 나가기 */
    @DeleteMapping("/rooms/{roomId}/leave")
    public ResponseEntity<ApiResponse<Void>> leaveRoom(
            @PathVariable Long roomId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Member me = memberService.getById(userDetails.getId());
        DirectRoom room = directMessageService.findById(roomId)
                .orElseThrow(() -> new com.ssafynity.demo.common.exception.BusinessException(
                        com.ssafynity.demo.common.exception.ErrorCode.DM_ROOM_NOT_FOUND));
        directMessageService.leaveRoom(room, me);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    /** 멤버 추가 (그룹 방) */
    @PostMapping("/rooms/{roomId}/members/{memberId}")
    public ResponseEntity<ApiResponse<Void>> addMember(
            @PathVariable Long roomId,
            @PathVariable Long memberId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        DirectRoom room = directMessageService.findById(roomId)
                .orElseThrow(() -> new com.ssafynity.demo.common.exception.BusinessException(
                        com.ssafynity.demo.common.exception.ErrorCode.DM_ROOM_NOT_FOUND));
        Member newMember = memberService.getById(memberId);
        directMessageService.addMember(room, newMember);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
