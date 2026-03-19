package com.ssafynity.demo.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tech_video")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class TechVideo {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** YouTube 영상 ID (https://youtu.be/{id}) */
    @Column(nullable = false)
    private String youtubeId;

    /** 영상 길이 표시용 (예: "15:30") */
    private String duration;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private Member author;

    /** 강의, 세미나, 코드리뷰, 프로젝트발표, 기타 */
    @Builder.Default
    private String category = "기타";

    /** 콤마 구분 태그 */
    @Column(columnDefinition = "TEXT")
    private String tags;

    @Builder.Default
    private int viewCount = 0;

    @Builder.Default
    private boolean pinned = false;

    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
