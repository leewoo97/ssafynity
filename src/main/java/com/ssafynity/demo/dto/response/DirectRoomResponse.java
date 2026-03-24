package com.ssafynity.demo.dto.response;

import com.ssafynity.demo.domain.DirectRoom;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class DirectRoomResponse {

    private Long id;
    private String type;  // DM / GROUP
    private String name;
    private LocalDateTime createdAt;

    /** DM 방일 때 상대방 정보 */
    private MemberResponse otherMember;

    /** 방 멤버 목록 */
    private List<MemberResponse> members;

    /** 마지막 메시지 (목록 페이지 미리보기용) */
    private String lastMessageContent;
    private LocalDateTime lastMessageAt;

    public static DirectRoomResponse from(DirectRoom room) {
        return DirectRoomResponse.builder()
                .id(room.getId())
                .type(room.getType())
                .name(room.getName())
                .createdAt(room.getCreatedAt())
                .build();
    }

    public static DirectRoomResponse from(
            DirectRoom room,
            com.ssafynity.demo.domain.Member otherMemberEntity,
            java.util.List<com.ssafynity.demo.domain.Member> memberEntities,
            com.ssafynity.demo.domain.DirectMessage lastMsg) {
        return DirectRoomResponse.builder()
                .id(room.getId())
                .type(room.getType())
                .name(room.getName())
                .createdAt(room.getCreatedAt())
                .otherMember(otherMemberEntity != null ? MemberResponse.of(otherMemberEntity) : null)
                .members(memberEntities != null ? memberEntities.stream().map(MemberResponse::of).toList() : null)
                .lastMessageContent(lastMsg != null ? lastMsg.getContent() : null)
                .lastMessageAt(lastMsg != null ? lastMsg.getCreatedAt() : null)
                .build();
    }
}
