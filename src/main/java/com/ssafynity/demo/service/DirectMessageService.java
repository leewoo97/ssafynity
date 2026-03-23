package com.ssafynity.demo.service;

import com.ssafynity.demo.domain.*;
import com.ssafynity.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DirectMessageService {

    private final DirectRoomRepository directRoomRepository;
    private final DirectRoomMemberRepository directRoomMemberRepository;
    private final DirectMessageRepository directMessageRepository;
    private final MemberRepository memberRepository;

    /** 1:1 DM 방 찾기 또는 생성 */
    @Transactional
    public DirectRoom findOrCreateDm(Member a, Member b) {
        return directRoomRepository.findDmBetween(a, b).orElseGet(() -> {
            DirectRoom room = directRoomRepository.save(
                    DirectRoom.builder().type("DM").creator(a).build());
            directRoomMemberRepository.save(DirectRoomMember.builder().room(room).member(a).build());
            directRoomMemberRepository.save(DirectRoomMember.builder().room(room).member(b).build());
            return room;
        });
    }

    /** 그룹 채팅 생성 */
    @Transactional
    public DirectRoom createGroup(Member creator, String name, List<Long> memberIds) {
        DirectRoom room = directRoomRepository.save(
                DirectRoom.builder().type("GROUP").name(name).creator(creator).build());
        // 생성자 추가
        directRoomMemberRepository.save(DirectRoomMember.builder().room(room).member(creator).build());
        // 초대 멤버 추가
        for (Long memberId : memberIds) {
            memberRepository.findById(memberId).ifPresent(m -> {
                if (!directRoomMemberRepository.existsByRoomAndMember(room, m)) {
                    directRoomMemberRepository.save(
                            DirectRoomMember.builder().room(room).member(m).build());
                }
            });
        }
        return room;
    }

    /** ID로 방 조회 */
    public Optional<DirectRoom> findById(Long id) {
        return directRoomRepository.findById(id);
    }

    /** 멤버의 모든 대화방 (최신순) */
    public List<DirectRoom> getRoomsForMember(Member member) {
        return directRoomRepository.findRoomsByMember(member).stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .collect(Collectors.toList());
    }

    /** 방의 참여자 Member 목록 */
    public List<Member> getMemberList(DirectRoom room) {
        return directRoomMemberRepository.findByRoom(room).stream()
                .map(DirectRoomMember::getMember)
                .collect(Collectors.toList());
    }

    /** 1:1 DM에서 상대방 멤버 반환 */
    public Member getOtherMember(DirectRoom room, Member me) {
        return getMemberList(room).stream()
                .filter(m -> !m.getId().equals(me.getId()))
                .findFirst()
                .orElse(null);
    }

    /** 참여자 여부 확인 */
    public boolean isMember(DirectRoom room, Member member) {
        return directRoomMemberRepository.existsByRoomAndMember(room, member);
    }

    /** 방 메시지 목록 (최근 100개, 오래된 것 먼저) */
    public List<DirectMessage> getMessages(DirectRoom room) {
        return directMessageRepository.findTop100ByRoomOrderByCreatedAtAsc(room);
    }

    /** 마지막 메시지 */
    public Optional<DirectMessage> getLastMessage(DirectRoom room) {
        return directMessageRepository.findTopByRoomOrderByCreatedAtDesc(room);
    }

    /** 메시지 저장 */
    @Transactional
    public DirectMessage saveMessage(DirectRoom room, Member sender, String content) {
        return directMessageRepository.save(DirectMessage.builder()
                .room(room).sender(sender).content(content).build());
    }

    /** 그룹 채팅에 멤버 초대 */
    @Transactional
    public void addMember(DirectRoom room, Member member) {
        if (!directRoomMemberRepository.existsByRoomAndMember(room, member)) {
            directRoomMemberRepository.save(
                    DirectRoomMember.builder().room(room).member(member).build());
        }
    }

    /** 대화방 나가기 */
    @Transactional
    public void leaveRoom(DirectRoom room, Member member) {
        directRoomMemberRepository.deleteByRoomAndMember(room, member);
    }
}
