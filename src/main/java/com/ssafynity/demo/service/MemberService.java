package com.ssafynity.demo.service;

import com.ssafynity.demo.common.exception.BusinessException;
import com.ssafynity.demo.common.exception.ErrorCode;
import com.ssafynity.demo.domain.Member;
import com.ssafynity.demo.dto.request.ProfileUpdateRequest;
import com.ssafynity.demo.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Member register(String username, String password, String nickname, String email,
                           String realName, String campus, Integer cohort, Integer classCode) {
        if (memberRepository.findByUsername(username).isPresent()) {
            throw new BusinessException(ErrorCode.DUPLICATE_USERNAME);
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

    public Member getById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
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
    public void updateProfile(Long memberId, ProfileUpdateRequest req) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        member.setNickname(req.getNickname());
        member.setEmail(req.getEmail());
        member.setBio(req.getBio());
        if (req.getProfileImageUrl() != null && !req.getProfileImageUrl().isBlank()) {
            member.setProfileImageUrl(req.getProfileImageUrl());
        }
        member.setRealName((req.getRealName() != null && !req.getRealName().isBlank()) ? req.getRealName() : null);
        member.setCampus((req.getCampus() != null && !req.getCampus().isBlank()) ? req.getCampus() : null);
        member.setCohort(req.getCohort());
        member.setClassCode(req.getClassCode());
    }

    @Transactional
    public void changePassword(Long memberId, String currentPassword, String newPassword) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        if (!passwordEncoder.matches(currentPassword, member.getPassword())) {
            throw new BusinessException(ErrorCode.WRONG_PASSWORD);
        }
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

    public boolean isSameClass(Member viewer, Member target) {
        if (viewer == null || target == null) return false;
        return target.getCampus() != null && target.getCampus().equals(viewer.getCampus())
                && target.getCohort() != null && target.getCohort().equals(viewer.getCohort())
                && target.getClassCode() != null && target.getClassCode().equals(viewer.getClassCode());
    }
}
