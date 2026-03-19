package com.ssafynity.demo.chat;

import com.ssafynity.demo.dto.ChatMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * Redis Pub/Sub 발행자 (Publisher)
 *
 * 채팅 메시지를 JSON 직렬화하여 Redis "chat" 토픽에 publish.
 * Redis 가 해당 토픽을 구독하는 모든 RedisSubscriber 에 메시지를 전달한다.
 * → 서버가 여러 대여도 모든 인스턴스가 동일한 메시지를 수신하므로
 *   자연스러운 수평 확장(Scale-out)이 된다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisPublisher {

    private final RedisTemplate<String, String> redisTemplate;
    private final ChannelTopic chatTopic;
    private final ObjectMapper objectMapper;

    /**
     * ChatMessageDto 를 JSON 으로 직렬화하여 Redis 토픽에 publish.
     *
     * @param dto 전송할 채팅 메시지
     */
    public void publish(ChatMessageDto dto) {
        try {
            String json = objectMapper.writeValueAsString(dto);
            redisTemplate.convertAndSend(chatTopic.getTopic(), json);
            log.debug("[Redis Publish] topic={} roomId={} type={}", chatTopic.getTopic(), dto.getRoomId(), dto.getType());
        } catch (Exception e) {
            log.error("[Redis Publish] 직렬화 실패: {}", e.getMessage(), e);
        }
    }
}
