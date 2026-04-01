package com.ssafynity.demo.chat.repository;

import com.ssafynity.demo.chat.domain.ChatMessage;
import com.ssafynity.demo.chat.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /** DB fallback: 해당 방의 최근 메시지 100개 (오름차순) */
    List<ChatMessage> findTop100ByRoomOrderByCreatedAtAsc(ChatRoom room);
}
