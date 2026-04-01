package com.ssafynity.demo.chat.dto.response;

import com.ssafynity.demo.chat.domain.ChatRoom;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatRoomResponse {

    private Long id;
    private String name;
    private String description;
    private int activeUsers;
    private LocalDateTime createdAt;

    private Long creatorId;
    private String creatorNickname;

    public static ChatRoomResponse from(ChatRoom room) {
        return ChatRoomResponse.builder()
                .id(room.getId())
                .name(room.getName())
                .description(room.getDescription())
                .activeUsers(room.getActiveUsers())
                .createdAt(room.getCreatedAt())
                .creatorId(room.getCreator() != null ? room.getCreator().getId() : null)
                .creatorNickname(room.getCreator() != null ? room.getCreator().getNickname() : null)
                .build();
    }
}
