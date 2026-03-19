package com.ssafynity.demo.service;

import com.ssafynity.demo.domain.ChatMessage;
import com.ssafynity.demo.domain.ChatRoom;
import com.ssafynity.demo.domain.Member;
import com.ssafynity.demo.dto.ChatMessageDto;
import com.ssafynity.demo.repository.ChatMessageRepository;
import com.ssafynity.demo.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 채팅 메시지 서비스
 *
 * ① DB 영구 저장  : 모든 CHAT 타입 메시지를 MySQL(H2)에 저장
 * ② Redis 캐싱    : 방당 최근 N개 메시지를 List 에 유지
 *    key   = "chat:history:{roomId}"
 *    value = ChatMessageDto JSON 문자열
 *    사용자가 채팅방 입장 시 DB가 아닌 Redis에서 빠르게 이전 대화 로드.
 *    캐시 miss 발생 시 DB fallback → 캐시 워밍.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String HISTORY_KEY_PREFIX = "chat:history:";

    @Value("${chat.history.max-size:100}")
    private long maxHistorySize;

    /**
     * 메시지를 DB에 저장하고 Redis 캐시에도 추가.
     *
     * @param dto      전송 DTO (roomId, senderId, content, type 필수)
     * @param sender   발신자 Member (SYSTEM 메시지면 null)
     * @param room     채팅방
     */
    @Transactional
    public void saveMessage(ChatMessageDto dto, Member sender, ChatRoom room) {
        // 1. DB 영구 저장 (CHAT 타입만 저장; JOIN/LEAVE는 선택적)
        if ("CHAT".equals(dto.getType()) || "SYSTEM".equals(dto.getType())) {
            ChatMessage entity = ChatMessage.builder()
                    .room(room)
                    .sender(sender)
                    .content(dto.getContent())
                    .messageType(dto.getType())
                    .build();
            chatMessageRepository.save(entity);
        }

        // 2. Redis 캐시 (List rightPush → 오래된 것부터 최신 순)
        cacheMessage(dto);
    }

    /**
     * 채팅방의 최근 메시지 목록 반환.
     * Redis 캐시 hit → Redis 에서 반환.
     * Redis 캐시 miss → DB 에서 로드 후 캐시 워밍.
     */
    public List<ChatMessageDto> getRecentMessages(Long roomId) {
        String key = HISTORY_KEY_PREFIX + roomId;
        List<String> cached = redisTemplate.opsForList().range(key, 0, -1);

        if (cached != null && !cached.isEmpty()) {
            log.debug("[ChatHistory] Redis hit: roomId={} count={}", roomId, cached.size());
            return cached.stream()
                    .map(this::deserialize)
                    .filter(Objects::nonNull)
                    .toList();
        }

        // Cache miss → DB fallback
        log.debug("[ChatHistory] Redis miss: roomId={}, loading from DB", roomId);
        return loadFromDbAndWarm(roomId);
    }

    // ── private helpers ──────────────────────────────────────────────────────

    private void cacheMessage(ChatMessageDto dto) {
        String key = HISTORY_KEY_PREFIX + dto.getRoomId();
        try {
            String json = objectMapper.writeValueAsString(dto);
            redisTemplate.opsForList().rightPush(key, json);
            // 최대 N개만 유지 (오래된 메시지 trim)
            redisTemplate.opsForList().trim(key, -maxHistorySize, -1);
        } catch (Exception e) {
            log.error("[ChatHistory] Redis 캐시 저장 실패: {}", e.getMessage(), e);
        }
    }

    private List<ChatMessageDto> loadFromDbAndWarm(Long roomId) {
        return chatRoomRepository.findById(roomId).map(room -> {
            List<ChatMessage> messages = chatMessageRepository.findTop100ByRoomOrderByCreatedAtAsc(room);
            List<ChatMessageDto> dtos = messages.stream()
                    .map(this::toDto)
                    .toList();

            // 캐시 워밍
            String key = HISTORY_KEY_PREFIX + roomId;
            dtos.forEach(dto -> {
                try {
                    redisTemplate.opsForList().rightPush(key, objectMapper.writeValueAsString(dto));
                } catch (Exception ignored) {}
            });
            if (!dtos.isEmpty()) {
                redisTemplate.opsForList().trim(key, -maxHistorySize, -1);
            }
            return dtos;
        }).orElse(Collections.emptyList());
    }

    private ChatMessageDto toDto(ChatMessage msg) {
        return ChatMessageDto.builder()
                .type(msg.getMessageType())
                .roomId(msg.getRoom().getId())
                .senderId(msg.getSender() != null ? msg.getSender().getId() : null)
                .senderNickname(msg.getSender() != null ? msg.getSender().getNickname() : "시스템")
                .content(msg.getContent())
                .timestamp(msg.getCreatedAt().toString())
                .build();
    }

    private ChatMessageDto deserialize(String json) {
        try {
            return objectMapper.readValue(json, ChatMessageDto.class);
        } catch (Exception e) {
            log.warn("[ChatHistory] 역직렬화 실패: {}", e.getMessage());
            return null;
        }
    }
}
