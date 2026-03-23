package com.ssafynity.demo.service;

import com.ssafynity.demo.domain.Member;
import com.ssafynity.demo.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional
    public Member register(String username, String password, String nickname, String email,
                          String realName, String campus, Integer cohort, Integer classCode) {
        if (memberRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }
        Member member = Member.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .nickname(nickname)
                .email((email != null && !email.isBlank()) ? email : null)
                .realName((realName != null && !realName.isBlank()) ? realName : null)
                .campus((campus != null && !campus.isBlank()) ? campus : null)
                .cohort(cohort)
                .classCode(classCode)
                .role("USER")
                .build();
        return memberRepository.save(member);
    }

    public Optional<Member> findByUsername(String username) {
        return memberRepository.findByUsername(username);
    }

    public Optional<Member> findById(Long id) {
        return memberRepository.findById(id);
    }

    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    public boolean checkPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    @Transactional
    public void updateProfile(Long memberId, String nickname, String email, String bio,
                              String profileImageUrl, String realName,
                              String campus, Integer cohort, Integer classCode) {
        Member member = memberRepository.findById(memberId).orElseThrow();
        member.setNickname(nickname);
        member.setEmail(email);
        member.setBio(bio);
        if (profileImageUrl != null && !profileImageUrl.isBlank()) {
            member.setProfileImageUrl(profileImageUrl);
        }
        member.setRealName((realName != null && !realName.isBlank()) ? realName : null);
        member.setCampus((campus != null && !campus.isBlank()) ? campus : null);
        member.setCohort(cohort);
        member.setClassCode(classCode);
    }

    @Transactional
    public void changePassword(Long memberId, String newPassword) {
        Member member = memberRepository.findById(memberId).orElseThrow();
        member.setPassword(passwordEncoder.encode(newPassword));
    }

    @Transactional
    public void deleteMember(Long memberId) {
        memberRepository.deleteById(memberId);
    }

    public List<Member> findByCampusAndCohort(String campus, Integer cohort) {
        return memberRepository.findByCampusAndCohortOrderByNicknameAsc(campus, cohort);
    }

    public List<Member> findByCampus(String campus) {
        return memberRepository.findByCampusOrderByNicknameAsc(campus);
    }

    public List<Member> findByCampusAndCohortAndClassCode(String campus, Integer cohort, Integer classCode) {
        return memberRepository.findByCampusAndCohortAndClassCodeOrderByNicknameAsc(campus, cohort, classCode);
    }

    /**
     * viewer가 target의 실명을 볼 수 있는지:
     * 1. 같은 캠퍼스 + 같은 기수 + 같은 반
     * 2. 친구 관계 (FriendshipService에서 판단)
     */
    public boolean isSameClass(Member viewer, Member target) {
        if (viewer == null || target == null) return false;
        return target.getCampus() != null && target.getCampus().equals(viewer.getCampus())
                && target.getCohort() != null && target.getCohort().equals(viewer.getCohort())
                && target.getClassCode() != null && target.getClassCode().equals(viewer.getClassCode());
    }
}
