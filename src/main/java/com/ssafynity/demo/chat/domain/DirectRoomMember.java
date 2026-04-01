package com.ssafynity.demo.chat.domain;

import com.ssafynity.demo.domain.Member;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/** 채팅방 참여자 목록 (M:N bridge) */
@Entity
@Table(name = "direct_room_member",
        uniqueConstraints = @UniqueConstraint(columnNames = {"room_id", "member_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DirectRoomMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private DirectRoom room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    private LocalDateTime joinedAt;

    /** 마지막으로 이 방을 읽은 시각 (미읽음 카운트 계산에 사용) */
    private LocalDateTime lastReadAt;

    @PrePersist
    public void prePersist() {
        joinedAt = LocalDateTime.now();
    }
}
