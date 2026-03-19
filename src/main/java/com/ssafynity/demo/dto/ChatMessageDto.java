package com.ssafynity.demo.dto;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/**
 * WebSocket STOMP 메시지 전송 DTO.
 * Redis Pub/Sub 직렬화를 위해 Serializable 구현.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 메시지 타입
     * CHAT   - 일반 채팅
     * JOIN   - 입장
     * LEAVE  - 퇴장
     * SYSTEM - 시스템 공지
     */
    private String type;

    /** 채팅방 ID */
    private Long roomId;

    /** 발신자 Member ID (SYSTEM 메시지는 null) */
    private Long senderId;

    /** 발신자 닉네임 */
    private String senderNickname;

    /** 메시지 본문 */
    private String content;

    /** 서버 타임스탬프 (ISO-8601) */
    private String timestamp;
}
