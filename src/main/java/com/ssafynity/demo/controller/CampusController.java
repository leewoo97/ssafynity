package com.ssafynity.demo.controller;

import com.ssafynity.demo.domain.Member;
import com.ssafynity.demo.domain.Post;
import com.ssafynity.demo.service.FriendshipService;
import com.ssafynity.demo.service.MemberService;
import com.ssafynity.demo.service.PostService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 캠퍼스 커뮤니티
 * GET  /campus              → 내 캠퍼스로 자동 리다이렉트
 * GET  /campus/select       → 캠퍼스 선택 (미등록 유저 등)
 * GET  /campus/{name}       → 캠퍼스 커뮤니티 허브
 * GET  /campus/{name}/board → 캠퍼스 전용 게시판
 * POST /campus/{name}/board → 게시글 작성
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/campus")
public class CampusController {

    private final MemberService memberService;
    private final PostService postService;
    private final FriendshipService friendshipService;

    private static final Map<String, String> CAMPUS_IMAGES = new LinkedHashMap<>();
    private static final Map<String, String> CAMPUS_EMOJI  = new LinkedHashMap<>();

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

    /** /campus → 내 캠퍼스 커뮤니티로 자동 이동 */
    @GetMapping
    public String myCampus(HttpSession session) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/member/login";
        Member fresh = memberService.findById(loginMember.getId()).orElseThrow();
        if (fresh.getCampus() == null || fresh.getCampus().isBlank()) {
            return "redirect:/campus/select";
        }
        return "redirect:/campus/" + URLEncoder.encode(fresh.getCampus(), StandardCharsets.UTF_8);
    }

    /** 캠퍼스 선택 페이지 */
    @GetMapping("/select")
    public String selectPage(HttpSession session, Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        model.addAttribute("loginMember", loginMember);
        model.addAttribute("campusImages", CAMPUS_IMAGES);
        model.addAttribute("campusEmojis", CAMPUS_EMOJI);
        return "campus/select";
    }

    /** 캠퍼스 커뮤니티 허브 */
    @GetMapping("/{campus}")
    public String campusCommunity(@PathVariable String campus, HttpSession session, Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/member/login";
        if (!CAMPUS_IMAGES.containsKey(campus)) return "redirect:/campus/select";

        Member fresh = memberService.findById(loginMember.getId()).orElseThrow();
        List<Member> allMembers = memberService.findByCampus(campus);
        List<Post> recentPosts = postService.getRecentCampusPosts(campus);

        // 기수별 그룹핑 (TreeMap → 오름차순)
        Map<Integer, List<Member>> membersByCohort = allMembers.stream()
                .filter(m -> m.getCohort() != null)
                .collect(Collectors.groupingBy(Member::getCohort, TreeMap::new, Collectors.toList()));

        // 친구 ID set — 멤버 카드 실명 표시용
        Set<Long> friendIds = friendshipService.getFriends(fresh).stream()
                .map(Member::getId)
                .collect(Collectors.toSet());

        model.addAttribute("loginMember", fresh);
        model.addAttribute("campus", campus);
        model.addAttribute("campusEmoji",  CAMPUS_EMOJI.getOrDefault(campus, "🎓"));
        model.addAttribute("campusImage",  CAMPUS_IMAGES.getOrDefault(campus, "https://picsum.photos/seed/ssafy/1200/500"));
        model.addAttribute("allMembers",   allMembers);
        model.addAttribute("memberCount",  allMembers.size());
        model.addAttribute("membersByCohort", membersByCohort);
        model.addAttribute("cohorts",      new TreeSet<>(membersByCohort.keySet()));
        model.addAttribute("recentPosts",  recentPosts);
        model.addAttribute("campusImages", CAMPUS_IMAGES);
        model.addAttribute("campusEmojis", CAMPUS_EMOJI);
        model.addAttribute("isMyCampus",   campus.equals(fresh.getCampus()));
        model.addAttribute("friendIds",    friendIds);
        return "campus/community";
    }

    /** 캠퍼스 전용 게시판 — 목록 */
    @GetMapping("/{campus}/board")
    public String campusBoard(@PathVariable String campus,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(required = false) String keyword,
                              HttpSession session, Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/member/login";
        if (!CAMPUS_IMAGES.containsKey(campus)) return "redirect:/campus/select";

        Member fresh = memberService.findById(loginMember.getId()).orElseThrow();
        Pageable pageable = PageRequest.of(page, 10, Sort.by("createdAt").descending());
        Page<Post> posts = (keyword != null && !keyword.isBlank())
                ? postService.findByCampusAndKeyword(campus, keyword, pageable)
                : postService.findByCampusPaged(campus, pageable);

        model.addAttribute("loginMember", fresh);
        model.addAttribute("campus", campus);
        model.addAttribute("campusEmoji", CAMPUS_EMOJI.getOrDefault(campus, "🎓"));
        model.addAttribute("posts", posts);
        model.addAttribute("keyword", keyword);
        model.addAttribute("isMyCampus", campus.equals(fresh.getCampus()));
        model.addAttribute("currentPage", page);
        return "campus/board";
    }

    /** 캠퍼스 전용 게시판 — 글 작성 */
    @PostMapping("/{campus}/board")
    public String createCampusPost(@PathVariable String campus,
                                   @RequestParam String title,
                                   @RequestParam String content,
                                   HttpSession session) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/member/login";
        Member fresh = memberService.findById(loginMember.getId()).orElseThrow();
        postService.createCampusPost(title, content, fresh, campus);
        return "redirect:/campus/" + URLEncoder.encode(campus, StandardCharsets.UTF_8) + "/board";
    }
}
