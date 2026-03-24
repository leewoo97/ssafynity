package com.ssafynity.demo.dto.response;

import com.ssafynity.demo.domain.Friendship;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class FriendshipResponse {

    private Long id;
    private String status;  // PENDING / ACCEPTED / REJECTED
    private LocalDateTime createdAt;

    private Long requesterId;
    private String requesterNickname;
    private String requesterProfileImageUrl;

    private Long receiverId;
    private String receiverNickname;
    private String receiverProfileImageUrl;

    public static FriendshipResponse from(Friendship friendship) {
        return FriendshipResponse.builder()
                .id(friendship.getId())
                .status(friendship.getStatus())
                .createdAt(friendship.getCreatedAt())
                .requesterId(friendship.getRequester().getId())
                .requesterNickname(friendship.getRequester().getNickname())
                .requesterProfileImageUrl(friendship.getRequester().getProfileImageUrl())
                .receiverId(friendship.getReceiver().getId())
                .receiverNickname(friendship.getReceiver().getNickname())
                .receiverProfileImageUrl(friendship.getReceiver().getProfileImageUrl())
                .build();
    }
}
