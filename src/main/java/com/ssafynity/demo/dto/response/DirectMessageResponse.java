package com.ssafynity.demo.dto.response;

import com.ssafynity.demo.domain.DirectMessage;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DirectMessageResponse {

    private Long id;
    private String content;
    private LocalDateTime createdAt;

    private Long senderId;
    private String senderNickname;
    private String senderProfileImageUrl;

    /** 아직 읽지 않은 참여자 수 (발신자 본인 제외) */
    private int unreadCount;

    public static DirectMessageResponse from(DirectMessage msg) {
        return DirectMessageResponse.builder()
                .id(msg.getId())
                .content(msg.getContent())
                .createdAt(msg.getCreatedAt())
                .senderId(msg.getSender() != null ? msg.getSender().getId() : null)
                .senderNickname(msg.getSender() != null ? msg.getSender().getNickname() : null)
                .senderProfileImageUrl(msg.getSender() != null ? msg.getSender().getProfileImageUrl() : null)
                .unreadCount(0)
                .build();
    }

    public static DirectMessageResponse from(DirectMessage msg, int unreadCount) {
        return DirectMessageResponse.builder()
                .id(msg.getId())
                .content(msg.getContent())
                .createdAt(msg.getCreatedAt())
                .senderId(msg.getSender() != null ? msg.getSender().getId() : null)
                .senderNickname(msg.getSender() != null ? msg.getSender().getNickname() : null)
                .senderProfileImageUrl(msg.getSender() != null ? msg.getSender().getProfileImageUrl() : null)
                .unreadCount(unreadCount)
                .build();
    }
}
