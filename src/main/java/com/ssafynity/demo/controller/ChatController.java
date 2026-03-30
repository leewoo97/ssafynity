package com.ssafynity.demo.controller;

import com.ssafynity.demo.chat.ChatSessionRegistry;
import com.ssafynity.demo.chat.RedisPublisher;
import com.ssafynity.demo.domain.ChatRoom;
import com.ssafynity.demo.domain.Member;
import com.ssafynity.demo.dto.ChatMessageDto;
import com.ssafynity.demo.security.CustomUserDetails;
import com.ssafynity.demo.service.ChatMessageService;
import com.ssafynity.demo.service.ChatRoomService;
import com.ssafynity.demo.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * WebSocket STOMP 채팅 컨트롤러
 *
 * 클라이언트 → 서버 메시지 경로:
 *   /app/chat.message  → sendMessage()   : 일반 채팅 전송
 *   /app/chat.join     → joinRoom()      : 입장
 *   /app/chat.leave    → leaveRoom()     : 퇴장
 *
 * 메시지는 RedisPublisher 를 통해 Redis "chat" 토픽에 publish 되고,
 * RedisSubscriber 가 수신하여 STOMP /topic/chat/{roomId} 로 브로드캐스트.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {

    private final RedisPublisher redisPublisher;
    private final ChatMessageService chatMessageService;
    private final ChatRoomService chatRoomService;
    private final MemberService memberService;
    private final ChatSessionRegistry chatSessionRegistry;

    /**
     * 일반 채팅 메시지 전송
     * payload: { type:"CHAT", roomId:1, content:"안녕하세요" }
     */
    @MessageMapping("/chat.message")
    public void sendMessage(@Payload ChatMessageDto dto, SimpMessageHeaderAccessor headerAccessor) {
        Member sender = getLoginMember(headerAccessor);
        if (sender == null) {
            log.warn("[ChatController] 비로그인 사용자의 메시지 전송 시도 roomId={}", dto.getRoomId());
            return;
        }

        Optional<ChatRoom> roomOpt = chatRoomService.findById(dto.getRoomId());
        if (roomOpt.isEmpty()) {
            log.warn("[ChatController] 존재하지 않는 채팅방 roomId={}", dto.getRoomId());
            return;
        }

        // DTO 완성 (서버에서 sender 정보 + timestamp 보강)
        dto.setType("CHAT");
        dto.setSenderId(sender.getId());
        dto.setSenderNickname(sender.getNickname());
        dto.setTimestamp(LocalDateTime.now().toString());

        // DB 저장 + Redis 캐시
        chatMessageService.saveMessage(dto, sender, roomOpt.get());

        // Redis Pub/Sub → 전체 브로드캐스트
        redisPublisher.publish(dto);

        log.debug("[ChatController] 메시지 전송 roomId={} sender={}", dto.getRoomId(), sender.getNickname());
    }

    /**
     * 채팅방 입장
     * payload: { type:"JOIN", roomId:1 }
     */
    @MessageMapping("/chat.join")
    public void joinRoom(@Payload ChatMessageDto dto, SimpMessageHeaderAccessor headerAccessor) {
        System.out.println("채팅방들어옴");
        Member sender = getLoginMember(headerAccessor);
        String nickname = sender != null ? sender.getNickname() : "익명";

        // WebSocket 세션에 채팅방 ID·nickname 저장 (비정상 종료 시 EventListener가 활용)
        Map<String, Object> sessionAttrs = headerAccessor.getSessionAttributes();
        if (sessionAttrs != null) {
            sessionAttrs.put("roomId", dto.getRoomId());
            sessionAttrs.put("nickname", nickname);
        }

        // Redis에 세션 상태 저장 + 활성 유저 수 +1
        if (sender != null) {
            chatSessionRegistry.userJoin(headerAccessor.getSessionId(), dto.getRoomId(), sender.getId());
        }
        chatRoomService.incrementActiveUsers(dto.getRoomId());

        // 입장 알림 브로드캐스트
        ChatMessageDto joinMsg = ChatMessageDto.builder()
                .type("JOIN")
                .roomId(dto.getRoomId())
                .senderId(sender != null ? sender.getId() : null)
                .senderNickname(nickname)
                .content(nickname + "님이 입장했습니다.")
                .timestamp(LocalDateTime.now().toString())
                .build();

        redisPublisher.publish(joinMsg);
    }

    /**
     * 채팅방 퇴장
     * payload: { type:"LEAVE", roomId:1 }
     */
    @MessageMapping("/chat.leave")
    public void leaveRoom(@Payload ChatMessageDto dto, SimpMessageHeaderAccessor headerAccessor) {
        Member sender = getLoginMember(headerAccessor);
        String nickname = sender != null ? sender.getNickname() : "익명";

        // Redis 세션 상태 제거 + 활성 유저 수 -1
        // (여기서 제거하면 이후 SessionDisconnectEvent 가 발생해도 중복 처리 안 됨)
        if (sender != null) {
            chatSessionRegistry.userLeave(headerAccessor.getSessionId(), dto.getRoomId(), sender.getId());
        }
        chatRoomService.decrementActiveUsers(dto.getRoomId());

        // 퇴장 알림 브로드캐스트
        ChatMessageDto leaveMsg = ChatMessageDto.builder()
                .type("LEAVE")
                .roomId(dto.getRoomId())
                .senderId(sender != null ? sender.getId() : null)
                .senderNickname(nickname)
                .content(nickname + "님이 퇴장했습니다.")
                .timestamp(LocalDateTime.now().toString())
                .build();

        redisPublisher.publish(leaveMsg);
    }

    // ── private helpers ──────────────────────────────────────────────────────
    private Member getLoginMember(SimpMessageHeaderAccessor h) {
        if (h.getUser() == null) return null;
        UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) h.getUser();
        CustomUserDetails ud = (CustomUserDetails) auth.getPrincipal();
        return memberService.findById(ud.getId()).orElse(null);
    }
}
