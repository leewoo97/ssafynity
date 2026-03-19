package com.ssafynity.demo.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tech_doc")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class TechDoc {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    /** 마크다운 지원 - 뷰에서 렌더링 */
    @Column(name = "is_markdown")
    @Builder.Default
    private boolean markdown = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private Member author;

    /** 튜토리얼, 아키텍처, 알고리즘, DevOps, 데이터베이스, 프론트엔드, 백엔드, 기타 */
    @Builder.Default
    private String category = "기타";

    /** 콤마 구분 태그 (예: Java,Spring,JPA) */
    @Column(columnDefinition = "TEXT")
    private String tags;

    @Builder.Default
    private int viewCount = 0;

    /** 관리자 추천 고정 여부 */
    @Builder.Default
    private boolean pinned = false;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
