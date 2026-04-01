package com.ssafynity.demo.chat.repository;

import com.ssafynity.demo.chat.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    /** 최신 순으로 채팅방 목록 조회 */
    List<ChatRoom> findAllByOrderByCreatedAtDesc();
}
