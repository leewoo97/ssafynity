package com.ssafynity.demo.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mentor_profile")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentorProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 멘토 회원 (1:1) */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private Member member;

    /** 멘토링 제목 (예: "Spring Boot 백엔드 멘토링") */
    @Column(nullable = false)
    private String title;

    /** 경력/이력 (예: "SSAFY 12기 수료, 현 N사 백엔드 개발자 2년") */
    @Column(columnDefinition = "TEXT")
    private String career;

    /** 전문 분야 (콤마 구분, 예: "Spring Boot,JPA,AWS,알고리즘") */
    @Column(columnDefinition = "TEXT")
    private String specialties;

    /** 멘토 소개 (상세 자기소개) */
    @Column(columnDefinition = "TEXT")
    private String mentorBio;

    /** 최대 동시 멘티 수 */
    @Builder.Default
    private int maxMentees = 5;

    /** 현재 활성 멘티 수 */
    @Builder.Default
    private int currentMentees = 0;

    /** 완료한 총 멘토링 세션 수 */
    @Builder.Default
    private int sessionCount = 0;

    /** 멘토 활성 여부 (비활성이면 목록에서 숨김) */
    @Builder.Default
    @Column(name = "is_active")
    private boolean active = true;

    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
