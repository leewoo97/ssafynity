package com.ssafynity.demo.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "community_event")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Event {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    /** ONLINE / OFFLINE / HYBRID */
    @Builder.Default
    private String location = "ONLINE";

    /** 스터디, 해커톤, 세미나, 워크숍, 기타 */
    @Builder.Default
    private String eventType = "기타";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id")
    private Member organizer;

    @Builder.Default
    private int maxParticipants = 0; // 0 = 제한없음

    @Builder.Default
    private int currentParticipants = 0;

    /** UPCOMING, ONGOING, COMPLETED, CANCELLED */
    @Builder.Default
    private String status = "UPCOMING";

    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
