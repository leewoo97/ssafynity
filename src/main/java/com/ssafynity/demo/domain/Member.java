package com.ssafynity.demo.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String nickname;

    @Column
    private String email;

    @Column(columnDefinition = "TEXT")
    private String bio;

    /** 프로필 이미지 URL */
    private String profileImageUrl;

    /** 실명 (선택) */
    private String realName;

    /**
     * SSAFY 캠퍼스
     * 서울 / 대전 / 광주 / 구미 / 부울경
     */
    private String campus;

    /**
     * SSAFY 기수 (1~16)
     */
    private Integer cohort;

    /**
     * 반 번호 (1학기 기준 1, 2, 3 ...)
     */
    private Integer classCode;

    @Builder.Default
    @Column(nullable = false)
    private String role = "USER"; // USER, ADMIN

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        if (role == null) role = "USER";
    }
}
