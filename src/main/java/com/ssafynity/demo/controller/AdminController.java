package com.ssafynity.demo.controller;

import com.ssafynity.demo.domain.Member;
import com.ssafynity.demo.service.*;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final MemberService memberService;
    private final PostService postService;
    private final CommentService commentService;
    private final ReportService reportService;
    private final TechDocService techDocService;
    private final TechVideoService techVideoService;
    private final EventService eventService;
    private final ProjectService projectService;

    private boolean isAdmin(HttpSession session) {
        Member member = (Member) session.getAttribute("loginMember");
        return member != null && "ADMIN".equals(member.getRole());
    }

    @GetMapping
    public String dashboard(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/";
        model.addAttribute("memberCount", memberService.findAll().size());
        model.addAttribute("postCount", postService.findAll().size());
        model.addAttribute("commentCount", commentService.countAll());
        model.addAttribute("docCount", techDocService.findAll().size());
        model.addAttribute("videoCount", techVideoService.findAll().size());
        model.addAttribute("eventCount", eventService.findAll().size());
        model.addAttribute("projectCount", projectService.findAll().size());
        model.addAttribute("pendingReports", reportService.findPending());
        model.addAttribute("topViewed", postService.getTopViewedPosts());
        model.addAttribute("topLiked", postService.getTopLikedPosts());
        return "admin/dashboard";
    }

    @GetMapping("/members")
    public String members(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/";
        model.addAttribute("members", memberService.findAll());
        return "admin/members";
    }

    @PostMapping("/members/{id}/delete")
    public String deleteMember(@PathVariable Long id, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/";
        memberService.deleteMember(id);
        return "redirect:/admin/members";
    }

    @GetMapping("/posts")
    public String posts(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/";
        model.addAttribute("posts", postService.findAll());
        return "admin/posts";
    }

    @PostMapping("/posts/{id}/delete")
    public String deletePost(@PathVariable Long id, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/";
        postService.deletePost(id);
        return "redirect:/admin/posts";
    }

    @PostMapping("/reports/{id}/resolve")
    public String resolveReport(@PathVariable Long id, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/";
        reportService.resolve(id);
        return "redirect:/admin";
    }
}
