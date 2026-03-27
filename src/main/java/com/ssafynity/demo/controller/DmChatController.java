package com.ssafynity.demo.controller;

import com.ssafynity.demo.chat.RedisPublisher;
import com.ssafynity.demo.domain.DirectRoom;
import com.ssafynity.demo.domain.Member;
import com.ssafynity.demo.dto.ChatMessageDto;
import com.ssafynity.demo.security.CustomUserDetails;
import com.ssafynity.demo.service.DirectMessageService;
import com.ssafynity.demo.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

/**
 * DM / 그룹 채팅 WebSocket STOMP 컨트롤러
 *
 * 클라이언트 → /app/dm.send  → sendDm()
 *   └─ 검증 후 DB 저장 → Redis publish (channel="DM")
 *      └─ RedisSubscriber → /topic/dm/{roomId} 브로드캐스트
 *
 * 클라이언트 구독: /topic/dm/{roomId}
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class DmChatController {

    private final RedisPublisher redisPublisher;
    private final DirectMessageService directMessageService;
    private final MemberService memberService;
    private final SimpMessageSendingOperations messagingTemplate;

    /** DM / 그룹 메시지 전송 */
    @MessageMapping("/dm.send")
    public void sendDm(@Payload ChatMessageDto dto, SimpMessageHeaderAccessor headerAccessor) {
        Member fresh = getMember(headerAccessor);
        if (fresh == null) return;

        DirectRoom room = directMessageService.findById(dto.getRoomId()).orElse(null);
        if (room == null) return;

        // 참여자 아닌 경우 무시
        if (!directMessageService.isMember(room, fresh)) {
            log.warn("[DmChatController] 비참여자 메시지 시도 roomId={} sender={}", dto.getRoomId(), fresh.getNickname());
            return;
        }

        // DB 저장
        directMessageService.saveMessage(room, fresh, dto.getContent());

        // DTO 완성 후 Redis publish (channel=DM → RedisSubscriber가 /topic/dm/{id}로 라우팅)
        dto.setType("CHAT");
        dto.setChannel("DM");
        dto.setSenderId(fresh.getId());
        dto.setSenderNickname(fresh.getNickname());
        dto.setTimestamp(LocalDateTime.now().toString());

        redisPublisher.publish(dto);
        log.debug("[DmChatController] 전송 roomId={} sender={}", dto.getRoomId(), fresh.getNickname());
    }

    /** 입장 알림 — lastReadAt 갱신 후 READ 이벤트 브로드캐스트 */
    @MessageMapping("/dm.join")
    public void joinDm(@Payload ChatMessageDto dto, SimpMessageHeaderAccessor headerAccessor) {
        Member m = getMember(headerAccessor);
        String nickname = m != null ? m.getNickname() : "누군가";

        // 읽음 처리
        if (m != null && dto.getRoomId() != null) {
            DirectRoom room = directMessageService.findById(dto.getRoomId()).orElse(null);
            if (room != null && directMessageService.isMember(room, m)) {
                LocalDateTime readAt = directMessageService.markAsRead(room, m);
                ChatMessageDto readEvent = ChatMessageDto.builder()
                        .type("READ").channel("DM").roomId(dto.getRoomId())
                        .readerId(m.getId()).readAt(readAt.toString()).build();
                messagingTemplate.convertAndSend("/topic/dm/" + dto.getRoomId(), readEvent);
            }
        }

        dto.setType("JOIN");
        dto.setChannel("DM");
        dto.setSenderNickname(nickname);
        dto.setContent(nickname + "님이 입장했습니다.");
        dto.setTimestamp(LocalDateTime.now().toString());
        messagingTemplate.convertAndSend("/topic/dm/" + dto.getRoomId(), dto);
    }

    /** 명시적 읽음 처리 — /app/dm.read 구독 시 사용 */
    @MessageMapping("/dm.read")
    public void readDm(@Payload ChatMessageDto dto, SimpMessageHeaderAccessor headerAccessor) {
        Member m = getMember(headerAccessor);
        if (m == null || dto.getRoomId() == null) return;
        DirectRoom room = directMessageService.findById(dto.getRoomId()).orElse(null);
        if (room == null || !directMessageService.isMember(room, m)) return;

        LocalDateTime readAt = directMessageService.markAsRead(room, m);
        ChatMessageDto readEvent = ChatMessageDto.builder()
                .type("READ").channel("DM").roomId(dto.getRoomId())
                .readerId(m.getId()).readAt(readAt.toString()).build();
        messagingTemplate.convertAndSend("/topic/dm/" + dto.getRoomId(), readEvent);
    }

    // ── helper ──────────────────────────────────────────────────────────────────────
    private Member getMember(SimpMessageHeaderAccessor h) {
        if (h.getUser() == null) {
            log.warn("[DmChatController] h.getUser() == null → JWT 미인증 상태로 메시지 도달. CONNECT 시 토큰 확인 필요");
            return null;
        }
        try {
            UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) h.getUser();
            CustomUserDetails ud = (CustomUserDetails) auth.getPrincipal();
            return memberService.findById(ud.getId()).orElseGet(() -> {
                log.warn("[DmChatController] memberId={} DB에 없음", ud.getId());
                return null;
            });
        } catch (Exception e) {
            log.error("[DmChatController] getMember 오류: {}", e.getMessage(), e);
            return null;
        }
    }
}
