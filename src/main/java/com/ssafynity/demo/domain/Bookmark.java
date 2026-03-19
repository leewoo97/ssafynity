package com.ssafynity.demo.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookmark",
       uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "targetType", "targetId"}))
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Bookmark {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    /** POST, DOC, VIDEO, PROJECT */
    private String targetType;

    private Long targetId;

    /** 빠른 참조를 위한 제목 스냅샷 */
    private String targetTitle;

    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
