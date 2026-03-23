package com.ssafynity.demo.service;

import com.ssafynity.demo.domain.Friendship;
import com.ssafynity.demo.domain.Member;
import com.ssafynity.demo.repository.FriendshipRepository;
import com.ssafynity.demo.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final MemberRepository memberRepository;

    /** 친구 요청 전송 */
    @Transactional
    public void sendRequest(Member requester, Long receiverId) {
        Member receiver = memberRepository.findById(receiverId).orElseThrow();
        if (requester.getId().equals(receiverId)) return; // 자기 자신에게 요청 불가
        // 이미 관계가 있으면 무시
        if (friendshipRepository.findBetween(requester, receiver).isPresent()) return;
        friendshipRepository.save(Friendship.builder()
                .requester(requester)
                .receiver(receiver)
                .status("PENDING")
                .build());
    }

    /** 친구 요청 수락 */
    @Transactional
    public void accept(Long friendshipId, Member receiver) {
        Friendship f = friendshipRepository.findById(friendshipId).orElseThrow();
        if (!f.getReceiver().getId().equals(receiver.getId())) return; // 본인 확인
        f.setStatus("ACCEPTED");
    }

    /** 친구 요청 거절 */
    @Transactional
    public void reject(Long friendshipId, Member receiver) {
        Friendship f = friendshipRepository.findById(friendshipId).orElseThrow();
        if (!f.getReceiver().getId().equals(receiver.getId())) return;
        f.setStatus("REJECTED");
    }

    /** 친구 관계 삭제 */
    @Transactional
    public void unfriend(Member member, Long targetId) {
        Member target = memberRepository.findById(targetId).orElseThrow();
        friendshipRepository.findBetween(member, target).ifPresent(friendshipRepository::delete);
    }

    /** 두 멤버가 친구인지 */
    public boolean isFriend(Member a, Member b) {
        if (a == null || b == null) return false;
        return friendshipRepository.findBetween(a, b)
                .map(f -> "ACCEPTED".equals(f.getStatus()))
                .orElse(false);
    }

    /** 두 멤버 사이의 관계 상태 반환 (null / PENDING / ACCEPTED / REJECTED) */
    public String getStatus(Member viewer, Member target) {
        if (viewer == null || target == null) return null;
        return friendshipRepository.findBetween(viewer, target)
                .map(Friendship::getStatus)
                .orElse(null);
    }

    /** 두 멤버 사이의 Friendship ID 반환 (없으면 null) */
    public Long getFriendshipId(Member viewer, Member target) {
        if (viewer == null || target == null) return null;
        return friendshipRepository.findBetween(viewer, target)
                .map(Friendship::getId)
                .orElse(null);
    }

    /** 요청자인지 여부 — PENDING 중인 경우 */
    public boolean isRequester(Member viewer, Member target) {
        if (viewer == null || target == null) return false;
        return friendshipRepository.findBetween(viewer, target)
                .map(f -> "PENDING".equals(f.getStatus()) && f.getRequester().getId().equals(viewer.getId()))
                .orElse(false);
    }

    /** member가 받은 대기 중인 친구 요청 목록 */
    public List<Friendship> getPendingReceived(Member member) {
        return friendshipRepository.findByReceiverAndStatus(member, "PENDING");
    }

    /** member의 친구 목록 (ACCEPTED) */
    public List<Member> getFriends(Member member) {
        return friendshipRepository.findAcceptedFriendships(member).stream()
                .map(f -> f.getRequester().getId().equals(member.getId()) ? f.getReceiver() : f.getRequester())
                .collect(Collectors.toList());
    }

    /**
     * viewer가 target의 실명을 볼 수 있는지:
     * 1. 같은 캠퍼스 + 같은 기수 + 같은 반
     * 2. 친구 관계 (ACCEPTED)
     */
    public boolean canSeeRealName(Member viewer, Member target) {
        if (viewer == null || target == null) return false;
        if (target.getRealName() == null || target.getRealName().isBlank()) return false;
        // 같은 반이면 공개
        if (isSameClass(viewer, target)) return true;
        // 친구이면 공개
        return isFriend(viewer, target);
    }

    private boolean isSameClass(Member a, Member b) {
        return b.getCampus() != null && b.getCampus().equals(a.getCampus())
                && b.getCohort() != null && b.getCohort().equals(a.getCohort())
                && b.getClassCode() != null && b.getClassCode().equals(a.getClassCode());
    }
}
