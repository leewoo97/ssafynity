package com.ssafynity.demo.chat.repository;

import com.ssafynity.demo.chat.domain.DirectMessage;
import com.ssafynity.demo.chat.domain.DirectRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DirectMessageRepository extends JpaRepository<DirectMessage, Long> {

    /** 최근 100개 메시지 (오래된 것 먼저) */
    List<DirectMessage> findTop100ByRoomOrderByCreatedAtAsc(DirectRoom room);

    /** 마지막 메시지 (목록 미리보기용) */
    Optional<DirectMessage> findTopByRoomOrderByCreatedAtDesc(DirectRoom room);
}
