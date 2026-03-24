package com.ssafynity.demo.controller;

import com.ssafynity.demo.common.response.ApiResponse;
import com.ssafynity.demo.domain.Friendship;
import com.ssafynity.demo.domain.Member;
import com.ssafynity.demo.dto.response.FriendshipResponse;
import com.ssafynity.demo.dto.response.MemberResponse;
import com.ssafynity.demo.security.CustomUserDetails;
import com.ssafynity.demo.service.FriendshipService;
import com.ssafynity.demo.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class FriendController {

    private final FriendshipService friendshipService;
    private final MemberService memberService;

    /** 친구 목록 */
    @GetMapping
    public ResponseEntity<ApiResponse<List<MemberResponse>>> friends(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Member me = memberService.getById(userDetails.getId());
        List<MemberResponse> result = friendshipService.getFriends(me).stream()
                .map(MemberResponse::of).toList();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    /** 받은 친구 요청 목록 */
    @GetMapping("/requests/received")
    public ResponseEntity<ApiResponse<List<FriendshipResponse>>> received(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Member me = memberService.getById(userDetails.getId());
        List<FriendshipResponse> result = friendshipService.getPendingReceived(me).stream()
                .map(FriendshipResponse::from).toList();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    /** 친구 상태 조회 */
    @GetMapping("/status/{targetId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> status(
            @PathVariable Long targetId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Member me = memberService.getById(userDetails.getId());
        Member target = memberService.getById(targetId);
        String status = friendshipService.getStatus(me, target);
        Long friendshipId = friendshipService.getFriendshipId(me, target);
        boolean isRequester = friendshipService.isRequester(me, target);
        Map<String, Object> result = Map.of(
                "status", status,
                "friendshipId", friendshipId != null ? friendshipId : -1L,
                "isRequester", isRequester
        );
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    /** 친구 요청 보내기 */
    @PostMapping("/request/{receiverId}")
    public ResponseEntity<ApiResponse<Void>> sendRequest(
            @PathVariable Long receiverId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Member me = memberService.getById(userDetails.getId());
        friendshipService.sendRequest(me, receiverId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    /** 친구 요청 수락 */
    @PostMapping("/{friendshipId}/accept")
    public ResponseEntity<ApiResponse<Void>> accept(
            @PathVariable Long friendshipId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Member me = memberService.getById(userDetails.getId());
        friendshipService.accept(friendshipId, me);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    /** 친구 요청 거절 */
    @PostMapping("/{friendshipId}/reject")
    public ResponseEntity<ApiResponse<Void>> reject(
            @PathVariable Long friendshipId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Member me = memberService.getById(userDetails.getId());
        friendshipService.reject(friendshipId, me);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    /** 친구 삭제 */
    @DeleteMapping("/{targetId}")
    public ResponseEntity<ApiResponse<Void>> unfriend(
            @PathVariable Long targetId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Member me = memberService.getById(userDetails.getId());
        friendshipService.unfriend(me, targetId);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
