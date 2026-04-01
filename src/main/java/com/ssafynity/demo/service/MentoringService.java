package com.ssafynity.demo.service;

import com.ssafynity.demo.chat.domain.ChatRoom;
import com.ssafynity.demo.chat.service.ChatRoomService;
import com.ssafynity.demo.domain.*;
import com.ssafynity.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MentoringService {

    private final MentorProfileRepository mentorProfileRepository;
    private final MentoringRequestRepository mentoringRequestRepository;
    private final ChatRoomService chatRoomService;
    private final NotificationService notificationService;

    /** 메일 서버 미설정 시 null이 허용됩니다 */
    @Autowired(required = false)
    private EmailService emailService;

    // ── 멘토 등록 ─────────────────────────────────────────────────────────────
    @Transactional
    public MentorProfile registerMentor(Member member, String title, String career,
                                        String specialties, String mentorBio, int maxMentees) {
        if (mentorProfileRepository.existsByMember(member)) {
            throw new IllegalStateException("이미 멘토로 등록되어 있습니다.");
        }
        MentorProfile profile = MentorProfile.builder()
                .member(member)
                .title(title)
                .career(career)
                .specialties(specialties)
                .mentorBio(mentorBio)
                .maxMentees(maxMentees)
                .build();
        return mentorProfileRepository.save(profile);
    }

    // ── 멘토 프로필 수정 ──────────────────────────────────────────────────────
    @Transactional
    public void updateMentor(Long profileId, String title, String career,
                             String specialties, String mentorBio, int maxMentees) {
        MentorProfile profile = mentorProfileRepository.findById(profileId).orElseThrow();
        profile.setTitle(title);
        profile.setCareer(career);
        profile.setSpecialties(specialties);
        profile.setMentorBio(mentorBio);
        profile.setMaxMentees(maxMentees);
    }

    // ── 멘토 활성/비활성 토글 ────────────────────────────────────────────────
    @Transactional
    public void toggleActive(Long profileId) {
        MentorProfile profile = mentorProfileRepository.findById(profileId).orElseThrow();
        profile.setActive(!profile.isActive());
    }

    // ── 멘토 목록 조회 ───────────────────────────────────────────────────────
    public List<MentorProfile> findAllActive() {
        return mentorProfileRepository.findByActiveTrueOrderByCreatedAtDesc();
    }

    public List<MentorProfile> searchBySpecialty(String keyword) {
        if (keyword == null || keyword.isBlank()) return findAllActive();
        return mentorProfileRepository.findBySpecialtiesContainingIgnoreCaseAndActiveTrue(keyword);
    }

    public Optional<MentorProfile> findById(Long id) {
        return mentorProfileRepository.findById(id);
    }

    public Optional<MentorProfile> findByMember(Member member) {
        return mentorProfileRepository.findByMember(member);
    }

    public boolean isMentor(Member member) {
        return mentorProfileRepository.existsByMember(member);
    }

    // ── 멘토링 신청 ──────────────────────────────────────────────────────────
    @Transactional
    public MentoringRequest applyForMentoring(Member mentee, Long mentorProfileId, String message) {
        MentorProfile profile = mentorProfileRepository.findById(mentorProfileId).orElseThrow(
                () -> new IllegalArgumentException("멘토를 찾을 수 없습니다."));

        // 자기 자신에게 신청 불가
        if (profile.getMember().getId().equals(mentee.getId())) {
            throw new IllegalStateException("자신에게 멘토링을 신청할 수 없습니다.");
        }

        // 이미 PENDING 또는 ACCEPTED인 신청이 있으면 불가
        boolean alreadyApplied = mentoringRequestRepository
                .existsByMenteeAndMentorProfileAndStatusIn(mentee, profile, List.of("PENDING", "ACCEPTED"));
        if (alreadyApplied) {
            throw new IllegalStateException("이미 신청하셨거나 진행 중인 멘토링이 있습니다.");
        }

        // 정원 초과 확인
        if (profile.getCurrentMentees() >= profile.getMaxMentees()) {
            throw new IllegalStateException("멘토링 정원이 가득 찼습니다.");
        }

        MentoringRequest request = MentoringRequest.builder()
                .mentee(mentee)
                .mentorProfile(profile)
                .message(message)
                .build();
        MentoringRequest saved = mentoringRequestRepository.save(request);

        // 멘토에게 사이트 알림 발송
        notificationService.send(
                profile.getMember(),
                "🎓 " + mentee.getNickname() + "님이 멘토링을 신청했습니다.",
                "/mentoring/my"
        );

        // 멘토에게 이메일 발송
        if (emailService != null) {
            String mentorEmail = profile.getMember().getEmail();
            if (mentorEmail != null && !mentorEmail.isBlank()) {
                emailService.sendApplicationEmail(
                        mentorEmail,
                        profile.getMember().getNickname(),
                        mentee.getNickname(),
                        message
                );
            }
        }

        return saved;
    }

    // ── 신청 승낙 ────────────────────────────────────────────────────────────
    @Transactional
    public void acceptRequest(Long requestId, Member mentor, String reply) {
        MentoringRequest request = mentoringRequestRepository.findById(requestId).orElseThrow();
        if (!request.getMentorProfile().getMember().getId().equals(mentor.getId())) {
            throw new IllegalStateException("권한이 없습니다.");
        }
        if (!"PENDING".equals(request.getStatus())) {
            throw new IllegalStateException("이미 처리된 신청입니다.");
        }

        request.setStatus("ACCEPTED");
        request.setReply(reply);

        // 1:1 채팅방 생성
        String roomName = mentor.getNickname() + " ↔ " + request.getMentee().getNickname() + " 멘토링";
        String roomDesc = "멘토링 채팅방 — " + request.getMentorProfile().getTitle();
        ChatRoom chatRoom = chatRoomService.create(roomName, roomDesc, mentor);
        request.setChatRoomId(chatRoom.getId());

        // 현재 멘티 수 +1
        MentorProfile profile = request.getMentorProfile();
        profile.setCurrentMentees(profile.getCurrentMentees() + 1);
        profile.setSessionCount(profile.getSessionCount() + 1);

        // 멘티에게 사이트 알림 발송
        notificationService.send(
                request.getMentee(),
                "🎉 " + mentor.getNickname() + " 멘토님이 멘토링 신청을 승낙했습니다! 채팅에서 이야기하세요.",
                "/chat/" + chatRoom.getId()
        );

        // 멘티에게 이메일 발송
        if (emailService != null) {
            String menteeEmail = request.getMentee().getEmail();
            if (menteeEmail != null && !menteeEmail.isBlank()) {
                emailService.sendReplyEmail(
                        menteeEmail,
                        request.getMentee().getNickname(),
                        mentor.getNickname(),
                        true,
                        reply
                );
            }
        }
    }

    // ── 신청 거절 ────────────────────────────────────────────────────────────
    @Transactional
    public void rejectRequest(Long requestId, Member mentor, String reply) {
        MentoringRequest request = mentoringRequestRepository.findById(requestId).orElseThrow();
        if (!request.getMentorProfile().getMember().getId().equals(mentor.getId())) {
            throw new IllegalStateException("권한이 없습니다.");
        }
        if (!"PENDING".equals(request.getStatus())) {
            throw new IllegalStateException("이미 처리된 신청입니다.");
        }

        request.setStatus("REJECTED");
        request.setReply(reply);

        // 멘티에게 사이트 알림 발송
        notificationService.send(
                request.getMentee(),
                "😢 " + mentor.getNickname() + " 멘토님이 멘토링 신청을 검토했지만 아쉽게도 수락하지 못했습니다.",
                "/mentoring/my"
        );

        // 멘티에게 이메일 발송
        if (emailService != null) {
            String menteeEmail = request.getMentee().getEmail();
            if (menteeEmail != null && !menteeEmail.isBlank()) {
                emailService.sendReplyEmail(
                        menteeEmail,
                        request.getMentee().getNickname(),
                        mentor.getNickname(),
                        false,
                        reply
                );
            }
        }
    }

    // ── 멘토 기준: 받은 신청 목록 ───────────────────────────────────────────
    public List<MentoringRequest> findRequestsByMentor(Member mentor) {
        Optional<MentorProfile> profile = mentorProfileRepository.findByMember(mentor);
        return profile.map(mentoringRequestRepository::findByMentorProfileOrderByCreatedAtDesc)
                      .orElseGet(List::of);
    }

    // ── 멘티 기준: 보낸 신청 목록 ───────────────────────────────────────────
    public List<MentoringRequest> findRequestsByMentee(Member mentee) {
        return mentoringRequestRepository.findByMenteeOrderByCreatedAtDesc(mentee);
    }

    // ── PENDING 신청 수 (뱃지용) ────────────────────────────────────────────
    public long countPendingRequests(MentorProfile profile) {
        return mentoringRequestRepository.countByMentorProfileAndStatus(profile, "PENDING");
    }
}
