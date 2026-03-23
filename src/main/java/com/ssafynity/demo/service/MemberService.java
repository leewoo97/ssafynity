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
                          String realName, String realNameScope,
                          String campus, Integer cohort) {
        if (memberRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }
        Member member = Member.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .nickname(nickname)
                .email((email != null && !email.isBlank()) ? email : null)
                .realName((realName != null && !realName.isBlank()) ? realName : null)
                .realNameScope(realNameScope != null ? realNameScope : "NONE")
                .campus((campus != null && !campus.isBlank()) ? campus : null)
                .cohort(cohort)
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
                              String profileImageUrl, String realName, String realNameScope,
                              String campus, Integer cohort) {
        Member member = memberRepository.findById(memberId).orElseThrow();
        member.setNickname(nickname);
        member.setEmail(email);
        member.setBio(bio);
        if (profileImageUrl != null && !profileImageUrl.isBlank()) {
            member.setProfileImageUrl(profileImageUrl);
        }
        member.setRealName((realName != null && !realName.isBlank()) ? realName : null);
        member.setRealNameScope(realNameScope != null ? realNameScope : "NONE");
        member.setCampus((campus != null && !campus.isBlank()) ? campus : null);
        member.setCohort(cohort);
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

    /**
     * viewer가 target의 실명을 볼 수 있는지 여부
     * ALL    → 항상 공개
     * COHORT → viewer와 같은 캠퍼스+기수일 때만 공개
     * NONE   → 비공개
     */
    public boolean canSeeRealName(Member viewer, Member target) {
        if (target.getRealName() == null || target.getRealName().isBlank()) return false;
        String scope = target.getRealNameScope();
        if (scope == null || "NONE".equals(scope)) return false;
        if ("ALL".equals(scope)) return true;
        if ("COHORT".equals(scope) && viewer != null) {
            return target.getCampus() != null
                    && target.getCampus().equals(viewer.getCampus())
                    && target.getCohort() != null
                    && target.getCohort().equals(viewer.getCohort());
        }
        return false;
    }
}
