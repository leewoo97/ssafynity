package com.ssafynity.demo.chat.domain;

import com.ssafynity.demo.domain.Member;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * 1:1 DM 또는 그룹 채팅방 엔티티
 * type: DM (1:1) | GROUP (그룹)
 */
@Entity
@Table(name = "direct_room")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DirectRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** DM | GROUP */
    @Column(nullable = false, length = 10)
    private String type;

    /** 그룹 채팅방 이름 (DM이면 null) */
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private Member creator;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
