package com.ssafynity.demo.controller;

import com.ssafynity.demo.domain.Member;
import com.ssafynity.demo.domain.MentorProfile;
import com.ssafynity.demo.domain.MentoringRequest;
import com.ssafynity.demo.service.MentoringService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

/**
 * 멘토링 시스템 컨트롤러
 *
 * GET  /mentors                  → 멘토 목록
 * GET  /mentors/{id}             → 멘토 상세 프로필
 * GET  /mentors/register         → 멘토 등록 폼
 * POST /mentors/register         → 멘토 등록 처리
 * GET  /mentors/edit             → 내 멘토 프로필 수정 폼
 * POST /mentors/edit             → 멘토 프로필 수정 처리
 * POST /mentors/toggle-active    → 멘토 활성/비활성 전환
 * POST /mentoring/apply/{id}     → 멘토링 신청
 * GET  /mentoring/my             → 내 멘토링 대시보드
 * POST /mentoring/accept/{id}    → 신청 승낙
 * POST /mentoring/reject/{id}    → 신청 거절
 */
@Controller
@RequiredArgsConstructor
public class MentoringController {

    private final MentoringService mentoringService;

    // ── 멘토 목록 ──────────────────────────────────────────────────────────
    @GetMapping("/mentors")
    public String mentorList(@RequestParam(required = false) String keyword,
                             Model model, HttpSession session) {
        List<MentorProfile> mentors;
        if (keyword != null && !keyword.isBlank()) {
            mentors = mentoringService.searchBySpecialty(keyword);
        } else {
            mentors = mentoringService.findAllActive();
        }
        model.addAttribute("mentors", mentors);
        model.addAttribute("keyword", keyword);

        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember != null) {
            mentoringService.findByMember(loginMember)
                    .ifPresent(p -> model.addAttribute("myProfile", p));
        }
        return "mentors/list";
    }

    // ── 멘토 상세 프로필 ────────────────────────────────────────────────────
    @GetMapping("/mentors/{id}")
    public String mentorDetail(@PathVariable Long id, Model model, HttpSession session) {
        MentorProfile profile = mentoringService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("멘토를 찾을 수 없습니다."));
        model.addAttribute("mentorProfile", profile);

        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember != null) {
            model.addAttribute("loginMember", loginMember);
            boolean isSelf = profile.getMember().getId().equals(loginMember.getId());
            model.addAttribute("isSelf", isSelf);
            if (!isSelf) {
                boolean alreadyApplied = mentoringService.findRequestsByMentee(loginMember).stream()
                        .anyMatch(r -> r.getMentorProfile().getId().equals(id)
                                && List.of("PENDING", "ACCEPTED").contains(r.getStatus()));
                model.addAttribute("alreadyApplied", alreadyApplied);
            }
        }
        return "mentors/detail";
    }

    // ── 멘토 등록 폼 ────────────────────────────────────────────────────────
    @GetMapping("/mentors/register")
    public String registerForm(HttpSession session, Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/member/login";

        // 이미 멘토인 경우 수정 폼으로 리다이렉트
        Optional<MentorProfile> existing = mentoringService.findByMember(loginMember);
        if (existing.isPresent()) {
            model.addAttribute("profile", existing.get());
            return "mentors/register";
        }
        model.addAttribute("profile", null);
        return "mentors/register";
    }

    // ── 멘토 등록 처리 ──────────────────────────────────────────────────────
    @PostMapping("/mentors/register")
    public String register(@RequestParam String title,
                           @RequestParam(required = false) String career,
                           @RequestParam(required = false) String specialties,
                           @RequestParam(required = false) String mentorBio,
                           @RequestParam(defaultValue = "5") int maxMentees,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/member/login";

        try {
            MentorProfile profile = mentoringService.registerMentor(
                    loginMember, title, career, specialties, mentorBio, maxMentees);
            redirectAttributes.addFlashAttribute("success", "멘토로 등록되었습니다! 멘티들의 신청을 기다려보세요 🎓");
            return "redirect:/mentors/" + profile.getId();
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/mentors/register";
        }
    }

    // ── 멘토 프로필 수정 처리 ────────────────────────────────────────────────
    @PostMapping("/mentors/edit")
    public String edit(@RequestParam Long profileId,
                       @RequestParam String title,
                       @RequestParam(required = false) String career,
                       @RequestParam(required = false) String specialties,
                       @RequestParam(required = false) String mentorBio,
                       @RequestParam(defaultValue = "5") int maxMentees,
                       HttpSession session,
                       RedirectAttributes redirectAttributes) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/member/login";

        MentorProfile profile = mentoringService.findById(profileId).orElseThrow();
        if (!profile.getMember().getId().equals(loginMember.getId())) {
            return "redirect:/mentors";
        }

        mentoringService.updateMentor(profileId, title, career, specialties, mentorBio, maxMentees);
        redirectAttributes.addFlashAttribute("success", "멘토 프로필이 수정되었습니다.");
        return "redirect:/mentors/" + profileId;
    }

    // ── 멘토 활성/비활성 전환 ────────────────────────────────────────────────
    @PostMapping("/mentors/toggle-active")
    public String toggleActive(@RequestParam Long profileId,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/member/login";

        MentorProfile profile = mentoringService.findById(profileId).orElseThrow();
        if (!profile.getMember().getId().equals(loginMember.getId())) {
            return "redirect:/mentors";
        }

        mentoringService.toggleActive(profileId);
        String msg = profile.isActive() ? "멘토링 모집을 일시 중단했습니다." : "멘토링 모집을 재개했습니다.";
        redirectAttributes.addFlashAttribute("success", msg);
        return "redirect:/mentoring/my";
    }

    // ── 멘토링 신청 ─────────────────────────────────────────────────────────
    @PostMapping("/mentoring/apply/{mentorProfileId}")
    public String apply(@PathVariable Long mentorProfileId,
                        @RequestParam(required = false) String message,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/member/login";

        try {
            mentoringService.applyForMentoring(loginMember, mentorProfileId, message);
            redirectAttributes.addFlashAttribute("success", "멘토링 신청이 완료됐습니다! 멘토님의 승낙을 기다려주세요 🙏");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/mentors/" + mentorProfileId;
    }

    // ── 내 멘토링 대시보드 ──────────────────────────────────────────────────
    @GetMapping("/mentoring/my")
    public String myMentoring(Model model, HttpSession session) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/member/login";

        // 멘토인 경우: 받은 신청 목록
        mentoringService.findByMember(loginMember).ifPresent(profile -> {
            model.addAttribute("myMentorProfile", profile);
            model.addAttribute("receivedRequests",
                    mentoringService.findRequestsByMentor(loginMember));
            model.addAttribute("pendingCount",
                    mentoringService.countPendingRequests(profile));
        });

        // 멘티로서 보낸 신청 목록
        model.addAttribute("sentRequests", mentoringService.findRequestsByMentee(loginMember));
        model.addAttribute("loginMember", loginMember);
        return "mentoring/my";
    }

    // ── 신청 승낙 ────────────────────────────────────────────────────────────
    @PostMapping("/mentoring/accept/{requestId}")
    public String accept(@PathVariable Long requestId,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/member/login";

        try {
            mentoringService.acceptRequest(requestId, loginMember);
            redirectAttributes.addFlashAttribute("success", "멘토링 신청을 승낙했습니다! 채팅방이 생성됐습니다. 🎉");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/mentoring/my";
    }

    // ── 신청 거절 ────────────────────────────────────────────────────────────
    @PostMapping("/mentoring/reject/{requestId}")
    public String reject(@PathVariable Long requestId,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/member/login";

        try {
            mentoringService.rejectRequest(requestId, loginMember);
            redirectAttributes.addFlashAttribute("success", "신청을 거절했습니다.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/mentoring/my";
    }
}
