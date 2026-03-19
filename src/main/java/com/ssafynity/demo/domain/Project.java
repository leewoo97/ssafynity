package com.ssafynity.demo.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "project")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Project {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    /** 콤마 구분 기술 스택 (예: Java,Spring Boot,React,MySQL) */
    @Column(columnDefinition = "TEXT")
    private String techStack;

    private String githubUrl;
    private String demoUrl;
    private String thumbnailUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private Member author;

    /** 팀 프로젝트 인원 수 */
    @Builder.Default
    private int teamSize = 1;

    /** IN_PROGRESS, COMPLETED */
    @Builder.Default
    private String status = "COMPLETED";

    @Builder.Default
    private int likeCount = 0;

    @Builder.Default
    private int viewCount = 0;

    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
