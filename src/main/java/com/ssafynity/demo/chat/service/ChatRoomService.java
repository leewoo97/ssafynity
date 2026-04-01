package com.ssafynity.demo.chat.service;

import com.ssafynity.demo.chat.ChatSessionRegistry;
import com.ssafynity.demo.chat.domain.ChatRoom;
import com.ssafynity.demo.chat.repository.ChatRoomRepository;
import com.ssafynity.demo.domain.Member;
import com.ssafynity.demo.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 채팅방 관리 서비스
 * 채팅방 CRUD + 활성 유저 수 관리
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatSessionRegistry chatSessionRegistry;
    private final MemberRepository memberRepository;

    /** 전체 채팅방 목록 (최신순) */
    public List<ChatRoom> findAll() {
        return chatRoomRepository.findAllByOrderByCreatedAtDesc();
    }

    /** ID로 채팅방 조회 */
    public Optional<ChatRoom> findById(Long id) {
        return chatRoomRepository.findById(id);
    }

    /** 채팅방 생성 */
    @Transactional
    public ChatRoom create(String name, String description, Member creator) {
        ChatRoom room = ChatRoom.builder()
                .name(name)
                .description(description)
                .creator(creator)
                .build();
        return chatRoomRepository.save(room);
    }

    /** 채팅방 삭제 (방장 또는 관리자만) */
    @Transactional
    public void delete(Long id) {
        chatRoomRepository.deleteById(id);
    }

    /** 입장 시 활성 유저 +1 */
    @Transactional
    public void incrementActiveUsers(Long roomId) {
        chatRoomRepository.findById(roomId).ifPresent(room -> {
            room.setActiveUsers(room.getActiveUsers() + 1);
        });
    }

    /** 퇴장 시 활성 유저 -1 */
    @Transactional
    public void decrementActiveUsers(Long roomId) {
        chatRoomRepository.findById(roomId).ifPresent(room -> {
            int current = room.getActiveUsers();
            room.setActiveUsers(Math.max(0, current - 1));
        });
    }

    /** Redis Set 기반 실시간 활성 유저 수 (SCARD) */
    public long getActiveUserCount(Long roomId) {
        return chatSessionRegistry.getActiveUserCount(roomId);
    }

    /** Redis SMEMBERS → userId Set → nickname 목록으로 변환 */
    public List<String> getActiveUserNicknames(Long roomId) {
        Set<String> userIdStrs = chatSessionRegistry.getActiveUserIds(roomId);
        if (userIdStrs.isEmpty()) return List.of();
        List<Long> ids = userIdStrs.stream().map(Long::parseLong).toList();
        return memberRepository.findAllById(ids).stream()
                .map(Member::getNickname)
                .collect(Collectors.toList());
    }

    /** Redis 전체 스캔 → roomId → userId Set */
    public java.util.Map<String, Set<String>> getAllActiveRoomUsers() {
        return chatSessionRegistry.getAllActiveRoomUsers();
    }

    /** userId 문자열 Set → nickname 목록 */
    public List<String> getNicknamesByIds(Set<String> userIdStrs) {
        if (userIdStrs.isEmpty()) return List.of();
        List<Long> ids = userIdStrs.stream().map(Long::parseLong).toList();
        return memberRepository.findAllById(ids).stream()
                .map(Member::getNickname)
                .collect(Collectors.toList());
    }
}
