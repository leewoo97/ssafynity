package com.ssafynity.demo.chat.domain;

import com.ssafynity.demo.domain.Member;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_message")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 채팅방 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private ChatRoom room;

    /** 발신자 (SYSTEM 메시지일 때는 null 가능) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private Member sender;

    /** 메시지 내용 */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    /**
     * 메시지 타입
     * CHAT   - 일반 채팅 메시지
     * JOIN   - 입장 알림
     * LEAVE  - 퇴장 알림
     * SYSTEM - 시스템 메시지
     */
    @Builder.Default
    @Column(nullable = false)
    private String messageType = "CHAT";

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
