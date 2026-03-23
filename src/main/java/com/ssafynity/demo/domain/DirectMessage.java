package com.ssafynity.demo.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/** 1:1 DM 및 그룹 채팅 메시지 */
@Entity
@Table(name = "direct_message")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DirectMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private DirectRoom room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private Member sender;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
