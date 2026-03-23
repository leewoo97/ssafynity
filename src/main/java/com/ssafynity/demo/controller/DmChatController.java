package com.ssafynity.demo.controller;

import com.ssafynity.demo.chat.RedisPublisher;
import com.ssafynity.demo.domain.DirectRoom;
import com.ssafynity.demo.domain.Member;
import com.ssafynity.demo.dto.ChatMessageDto;
import com.ssafynity.demo.service.DirectMessageService;
import com.ssafynity.demo.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.Map;

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
        Map<String, Object> attrs = headerAccessor.getSessionAttributes();
        if (attrs == null) return;
        Member sender = (Member) attrs.get("loginMember");
        if (sender == null) return;

        Member fresh = memberService.findById(sender.getId()).orElse(null);
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

    /** 입장 알림 (시스템 메시지, Redis 우회하여 직접 브로드캐스트) */
    @MessageMapping("/dm.join")
    public void joinDm(@Payload ChatMessageDto dto, SimpMessageHeaderAccessor headerAccessor) {
        Map<String, Object> attrs = headerAccessor.getSessionAttributes();
        String nickname = "누군가";
        if (attrs != null) {
            Member sender = (Member) attrs.get("loginMember");
            if (sender != null) nickname = sender.getNickname();
        }
        dto.setType("JOIN");
        dto.setChannel("DM");
        dto.setSenderNickname(nickname);
        dto.setContent(nickname + "님이 입장했습니다.");
        dto.setTimestamp(LocalDateTime.now().toString());
        messagingTemplate.convertAndSend("/topic/dm/" + dto.getRoomId(), dto);
    }
}
