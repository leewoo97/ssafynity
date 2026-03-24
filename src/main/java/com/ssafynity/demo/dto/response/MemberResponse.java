package com.ssafynity.demo.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ssafynity.demo.domain.Member;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 회원 응답 DTO.
 * realName 은 공개 조건(같은 반 / 친구)을 만족할 때만 포함된다.
 * canSeeRealName 플래그로 컨트롤한다.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MemberResponse {

    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String bio;
    private String profileImageUrl;
    private String campus;
    private Integer cohort;
    private Integer classCode;
    private String role;
    private LocalDateTime createdAt;

    /** 실명 공개 조건이 충족될 때만 포함 */
    private String realName;

    public static MemberResponse of(Member member) {
        return MemberResponse.builder()
                .id(member.getId())
                .username(member.getUsername())
                .nickname(member.getNickname())
                .email(member.getEmail())
                .bio(member.getBio())
                .profileImageUrl(member.getProfileImageUrl())
                .campus(member.getCampus())
                .cohort(member.getCohort())
                .classCode(member.getClassCode())
                .role(member.getRole())
                .createdAt(member.getCreatedAt())
                .build();
    }

    public static MemberResponse ofWithRealName(Member member) {
        return MemberResponse.builder()
                .id(member.getId())
                .username(member.getUsername())
                .nickname(member.getNickname())
                .email(member.getEmail())
                .bio(member.getBio())
                .profileImageUrl(member.getProfileImageUrl())
                .campus(member.getCampus())
                .cohort(member.getCohort())
                .classCode(member.getClassCode())
                .role(member.getRole())
                .createdAt(member.getCreatedAt())
                .realName(member.getRealName())
                .build();
    }
}
