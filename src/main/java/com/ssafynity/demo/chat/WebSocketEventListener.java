package com.ssafynity.demo.chat;

import com.ssafynity.demo.chat.dto.ChatMessageDto;
import com.ssafynity.demo.chat.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * WebSocket 세션 이벤트 리스너
 *
 * SessionDisconnectEvent 는 정상 퇴장(DISCONNECT 프레임)과
 * 비정상 종료(탭 닫기·네트워크 단절 → heartbeat timeout) 모두에서 발생한다.
 *
 * 처리 흐름:
 *   1. ChatSessionRegistry 에서 sessionId → (roomId, userId) 복원
 *   2. 활성 유저 카운트 감소 (ChatRoomService)
 *   3. 퇴장 알림 브로드캐스트 (RedisPublisher)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final ChatSessionRegistry chatSessionRegistry;
    private final ChatRoomService chatRoomService;
    private final RedisPublisher redisPublisher;

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = sha.getSessionId();

        if (sessionId == null) return;

        // ChatController.leaveRoom() 이 정상 호출됐다면 이미 Registry에서 제거됐으므로 empty 반환
        Optional<Long> roomIdOpt = chatSessionRegistry.userDisconnect(sessionId);
        if (roomIdOpt.isEmpty()) return;

        Long roomId = roomIdOpt.get();

        // 세션 attributes 에서 nickname 복원 (없으면 "알 수 없음")
        String nickname = "알 수 없음";
        if (sha.getSessionAttributes() != null) {
            Object nick = sha.getSessionAttributes().get("nickname");
            if (nick instanceof String s) nickname = s;
        }

        // 활성 유저 수 감소
        chatRoomService.decrementActiveUsers(roomId);

        // 퇴장 알림 브로드캐스트
        ChatMessageDto leaveMsg = ChatMessageDto.builder()
                .type("LEAVE")
                .roomId(roomId)
                .senderNickname(nickname)
                .content(nickname + "님이 연결이 끊어졌습니다.")
                .timestamp(LocalDateTime.now().toString())
                .build();
        redisPublisher.publish(leaveMsg);

        log.info("[WS Disconnect] 비정상 종료 처리 완료 sessionId={} roomId={} nickname={}", sessionId, roomId, nickname);
    }
}
