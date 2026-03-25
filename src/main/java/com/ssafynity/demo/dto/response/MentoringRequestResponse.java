package com.ssafynity.demo.dto.response;

import com.ssafynity.demo.domain.MentoringRequest;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MentoringRequestResponse {

    private Long id;
    private String message;
    private String status;
    private LocalDateTime createdAt;

    private Long menteeId;
    private String menteeNickname;
    private String menteeProfileImageUrl;

    private Long mentorProfileId;
    private String mentorTitle;
    private String mentorNickname;
    private String reply;
    private Long chatRoomId;

    public static MentoringRequestResponse from(MentoringRequest request) {
        return MentoringRequestResponse.builder()
                .id(request.getId())
                .message(request.getMessage())
                .status(request.getStatus())
                .createdAt(request.getCreatedAt())
                .menteeId(request.getMentee().getId())
                .menteeNickname(request.getMentee().getNickname())
                .menteeProfileImageUrl(request.getMentee().getProfileImageUrl())
                .mentorProfileId(request.getMentorProfile().getId())
                .mentorTitle(request.getMentorProfile().getTitle())
                .mentorNickname(request.getMentorProfile().getMember().getNickname())
                .reply(request.getReply())
                .chatRoomId(request.getChatRoomId())
                .build();
    }
}
