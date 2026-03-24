package com.ssafynity.demo.dto.response;

import com.ssafynity.demo.domain.MentorProfile;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MentorProfileResponse {

    private Long id;
    private String title;
    private String career;
    private String specialties;
    private String mentorBio;
    private int maxMentees;
    private int currentMentees;
    private int sessionCount;
    private boolean active;
    private LocalDateTime createdAt;

    private Long memberId;
    private String memberNickname;
    private String memberProfileImageUrl;
    private String memberCampus;
    private Integer memberCohort;

    public static MentorProfileResponse from(MentorProfile mentor) {
        return MentorProfileResponse.builder()
                .id(mentor.getId())
                .title(mentor.getTitle())
                .career(mentor.getCareer())
                .specialties(mentor.getSpecialties())
                .mentorBio(mentor.getMentorBio())
                .maxMentees(mentor.getMaxMentees())
                .currentMentees(mentor.getCurrentMentees())
                .sessionCount(mentor.getSessionCount())
                .active(mentor.isActive())
                .createdAt(mentor.getCreatedAt())
                .memberId(mentor.getMember().getId())
                .memberNickname(mentor.getMember().getNickname())
                .memberProfileImageUrl(mentor.getMember().getProfileImageUrl())
                .memberCampus(mentor.getMember().getCampus())
                .memberCohort(mentor.getMember().getCohort())
                .build();
    }
}
