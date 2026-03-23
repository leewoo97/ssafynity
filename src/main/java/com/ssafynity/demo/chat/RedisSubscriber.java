package com.ssafynity.demo.chat;

import com.ssafynity.demo.dto.ChatMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * Redis Pub/Sub 구독자 (Subscriber)
 *
 * Redis 로부터 "chat" 토픽 메시지를 수신하여
 * STOMP 브로커(/topic/chat/{roomId})로 브로드캐스트한다.
 *
 * 흐름:
 *   Redis topic "chat"
 *     └─▶ RedisSubscriber.onMessage()
 *           └─▶ messagingTemplate.convertAndSend("/topic/chat/{roomId}", dto)
 *                 └─▶ 해당 방 구독 중인 모든 WebSocket 클라이언트 수신
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisSubscriber implements MessageListener {

    private final SimpMessageSendingOperations messagingTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Redis 메시지 수신 콜백.
     * message.getBody() = RedisPublisher 가 publish 한 JSON bytes
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String body = new String(message.getBody());
            ChatMessageDto dto = objectMapper.readValue(body, ChatMessageDto.class);

            // channel 에 따라 STOMP 목적지 결정
            String prefix = "DM".equals(dto.getChannel()) ? "/topic/dm/" : "/topic/chat/";
            String destination = prefix + dto.getRoomId();
            messagingTemplate.convertAndSend(destination, dto);

            log.debug("[Redis Subscribe] → {} type={} sender={}", destination, dto.getType(), dto.getSenderNickname());
        } catch (Exception e) {
            log.error("[Redis Subscribe] 메시지 처리 실패: {}", e.getMessage(), e);
        }
    }
}
