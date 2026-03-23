package com.ssafynity.demo.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mentoring_request")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentoringRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 신청한 멘티 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentee_id", nullable = false)
    private Member mentee;

    /** 신청 대상 멘토 프로필 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_profile_id", nullable = false)
    private MentorProfile mentorProfile;

    /** 신청 메시지 */
    @Column(columnDefinition = "TEXT")
    private String message;

    /** 상태: PENDING / ACCEPTED / REJECTED */
    @Builder.Default
    @Column(nullable = false)
    private String status = "PENDING";

    /** 승낙 시 생성된 1:1 채팅방 ID */
    private Long chatRoomId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
