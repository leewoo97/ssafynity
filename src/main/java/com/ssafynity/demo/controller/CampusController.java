package com.ssafynity.demo.controller;

import com.ssafynity.demo.domain.Member;
import com.ssafynity.demo.service.MemberService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

/**
 * 캠퍼스 추억 공간
 * GET /campus          → 내 캠퍼스+기수 추억 공간 (로그인 필요)
 * GET /campus/browse   → 특정 캠퍼스+기수 조회 (퍼블릭)
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/campus")
public class CampusController {

    private final MemberService memberService;

    /** 캠퍼스별 대표 이미지 (picsum seed) */
    private static final Map<String, String> CAMPUS_IMAGES = new LinkedHashMap<>();
    private static final Map<String, String> CAMPUS_EMOJI = new LinkedHashMap<>();

    static {
        CAMPUS_IMAGES.put("서울",   "https://picsum.photos/seed/seoul-ssafy/1200/500");
        CAMPUS_IMAGES.put("대전",   "https://picsum.photos/seed/daejeon-ssafy/1200/500");
        CAMPUS_IMAGES.put("광주",   "https://picsum.photos/seed/gwangju-ssafy/1200/500");
        CAMPUS_IMAGES.put("구미",   "https://picsum.photos/seed/gumi-ssafy/1200/500");
        CAMPUS_IMAGES.put("부울경", "https://picsum.photos/seed/busan-ssafy/1200/500");

        CAMPUS_EMOJI.put("서울",   "🏙️");
        CAMPUS_EMOJI.put("대전",   "🌿");
        CAMPUS_EMOJI.put("광주",   "🌸");
        CAMPUS_EMOJI.put("구미",   "⚙️");
        CAMPUS_EMOJI.put("부울경", "🌊");
    }

    /** 내 캠퍼스 추억 공간 */
    @GetMapping
    public String myCampus(HttpSession session, Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/member/login";

        Member fresh = memberService.findById(loginMember.getId()).orElseThrow();

        if (fresh.getCampus() == null || fresh.getCohort() == null) {
            model.addAttribute("noCampus", true);
            model.addAttribute("loginMember", fresh);
            return "campus/space";
        }

        List<Member> cohortMembers = memberService.findByCampusAndCohort(
                fresh.getCampus(), fresh.getCohort());

        model.addAttribute("loginMember", fresh);
        model.addAttribute("campus", fresh.getCampus());
        model.addAttribute("cohort", fresh.getCohort());
        model.addAttribute("cohortMembers", cohortMembers);
        model.addAttribute("memberCount", cohortMembers.size());
        model.addAttribute("campusImage", CAMPUS_IMAGES.getOrDefault(fresh.getCampus(),
                "https://picsum.photos/seed/ssafy/1200/500"));
        model.addAttribute("campusEmoji", CAMPUS_EMOJI.getOrDefault(fresh.getCampus(), "🎓"));
        model.addAttribute("campusImages", CAMPUS_IMAGES);
        model.addAttribute("campusEmojis", CAMPUS_EMOJI);
        return "campus/space";
    }

    /** 특정 캠퍼스+기수 둘러보기 (누구나 접근 가능) */
    @GetMapping("/browse")
    public String browse(@RequestParam(required = false) String campus,
                         @RequestParam(required = false) Integer cohort,
                         HttpSession session, Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        model.addAttribute("loginMember", loginMember);
        model.addAttribute("campusImages", CAMPUS_IMAGES);
        model.addAttribute("campusEmojis", CAMPUS_EMOJI);
        model.addAttribute("campuses", List.of("서울", "대전", "광주", "구미", "부울경"));

        if (campus != null && !campus.isBlank() && cohort != null) {
            List<Member> members = memberService.findByCampusAndCohort(campus, cohort);
            model.addAttribute("browserCampus", campus);
            model.addAttribute("browserCohort", cohort);
            model.addAttribute("cohortMembers", members);
            model.addAttribute("memberCount", members.size());
            model.addAttribute("campusImage", CAMPUS_IMAGES.getOrDefault(campus,
                    "https://picsum.photos/seed/ssafy/1200/500"));
            model.addAttribute("campusEmoji", CAMPUS_EMOJI.getOrDefault(campus, "🎓"));
        }
        return "campus/browse";
    }
}
