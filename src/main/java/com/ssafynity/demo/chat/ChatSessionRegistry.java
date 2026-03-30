package com.ssafynity.demo.chat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * WebSocket 세션 ↔ 채팅방 매핑을 Redis에 저장/조회/삭제하는 레지스트리.
 *
 * Redis 키 구조:
 *   ws:session:{sessionId}:roomId   → String  (해당 세션이 속한 roomId, TTL 24h)
 *   ws:session:{sessionId}:userId   → String  (해당 세션의 userId,  TTL 24h)
 *   chat:room:{roomId}:users        → Set<String>  (현재 입장 중인 userId 목록)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatSessionRegistry {

    private static final String SESSION_ROOM_KEY  = "ws:session:%s:roomId";
    private static final String SESSION_USER_KEY  = "ws:session:%s:userId";
    private static final String ROOM_USERS_KEY    = "chat:room:%s:users";
    private static final Duration SESSION_TTL     = Duration.ofHours(24);

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 입장 처리: 세션 → (roomId, userId) 저장 + 방 활성 유저 Set에 추가
     */
    public void userJoin(String sessionId, Long roomId, Long userId) {
        String roomKey = String.format(SESSION_ROOM_KEY, sessionId);
        String userKey = String.format(SESSION_USER_KEY, sessionId);
        String usersKey = String.format(ROOM_USERS_KEY, roomId);

        redisTemplate.opsForValue().set(roomKey, roomId.toString(), SESSION_TTL);
        redisTemplate.opsForValue().set(userKey, userId.toString(), SESSION_TTL);
        redisTemplate.opsForSet().add(usersKey, userId.toString());

        log.debug("[Registry] JOIN sessionId={} roomId={} userId={}", sessionId, roomId, userId);
    }

    /**
     * 퇴장 처리: 세션 키 삭제 + 방 활성 유저 Set에서 제거
     */
    public void userLeave(String sessionId, Long roomId, Long userId) {
        redisTemplate.delete(String.format(SESSION_ROOM_KEY, sessionId));
        redisTemplate.delete(String.format(SESSION_USER_KEY, sessionId));
        redisTemplate.opsForSet().remove(String.format(ROOM_USERS_KEY, roomId), userId.toString());

        log.debug("[Registry] LEAVE sessionId={} roomId={} userId={}", sessionId, roomId, userId);
    }

    /**
     * 비정상 종료 처리: sessionId만으로 roomId·userId를 복원하여 정리.
     * SessionDisconnectEvent 핸들러에서 호출.
     *
     * @return 정리한 roomId (없으면 empty)
     */
    public Optional<Long> userDisconnect(String sessionId) {
        String roomKey = String.format(SESSION_ROOM_KEY, sessionId);
        String userKey = String.format(SESSION_USER_KEY, sessionId);

        String roomIdStr = redisTemplate.opsForValue().get(roomKey);
        String userIdStr = redisTemplate.opsForValue().get(userKey);

        redisTemplate.delete(roomKey);
        redisTemplate.delete(userKey);

        if (roomIdStr == null || userIdStr == null) {
            log.debug("[Registry] DISCONNECT sessionId={} → 세션 정보 없음 (이미 정상 퇴장)", sessionId);
            return Optional.empty();
        }

        Long roomId = Long.parseLong(roomIdStr);
        Long userId = Long.parseLong(userIdStr);
        redisTemplate.opsForSet().remove(String.format(ROOM_USERS_KEY, roomId), userIdStr);

        log.info("[Registry] DISCONNECT(비정상) sessionId={} roomId={} userId={}", sessionId, roomId, userId);
        return Optional.of(roomId);
    }

    /**
     * 특정 채팅방의 현재 활성 유저 수 (Redis Set의 SCARD).
     */
    public long getActiveUserCount(Long roomId) {
        Long count = redisTemplate.opsForSet().size(String.format(ROOM_USERS_KEY, roomId));
        return count != null ? count : 0L;
    }

    /**
     * 특정 채팅방에 접속 중인 userId Set 반환 (Redis SMEMBERS).
     */
    public Set<String> getActiveUserIds(Long roomId) {
        Set<String> members = redisTemplate.opsForSet().members(String.format(ROOM_USERS_KEY, roomId));
        return members != null ? members : Collections.emptySet();
    }

    /**
     * Redis에서 chat:room:*:users 키를 전체 스캔하여 roomId → userId Set 반환.
     * DB 채팅방 목록 없이도 현재 접속 중인 세션을 직접 조회.
     */
    public Map<String, Set<String>> getAllActiveRoomUsers() {
        Map<String, Set<String>> result = new LinkedHashMap<>();
        Set<String> keys = redisTemplate.keys("chat:room:*:users");
        if (keys == null) return result;
        for (String key : keys) {
            // key 형식: chat:room:{roomId}:users
            String[] parts = key.split(":");
            if (parts.length < 4) continue;
            String roomId = parts[2];
            Set<String> users = redisTemplate.opsForSet().members(key);
            result.put(roomId, users != null ? users : Collections.emptySet());
        }
        return result;
    }
}
