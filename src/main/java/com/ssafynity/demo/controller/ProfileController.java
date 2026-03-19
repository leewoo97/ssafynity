package com.ssafynity.demo.controller;

import com.ssafynity.demo.domain.Member;
import com.ssafynity.demo.service.CommentService;
import com.ssafynity.demo.service.MemberService;
import com.ssafynity.demo.service.PostService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/profile")
public class ProfileController {

    private final MemberService memberService;
    private final PostService postService;
    private final CommentService commentService;

    @GetMapping("/me")
    public String myPage(HttpSession session, Model model) {
        Member member = (Member) session.getAttribute("loginMember");
        if (member == null) return "redirect:/member/login";
        // 세션 멤버는 오래된 정보일 수 있으므로 DB에서 새로 조회
        Member fresh = memberService.findById(member.getId()).orElseThrow();
        model.addAttribute("member", fresh);
        model.addAttribute("myPosts", postService.findByAuthor(fresh));
        model.addAttribute("myComments", commentService.findByAuthor(fresh));
        return "member/mypage";
    }

    @GetMapping("/edit")
    public String editForm(HttpSession session, Model model) {
        Member member = (Member) session.getAttribute("loginMember");
        if (member == null) return "redirect:/member/login";
        Member fresh = memberService.findById(member.getId()).orElseThrow();
        model.addAttribute("member", fresh);
        return "member/edit";
    }

    @PostMapping("/edit")
    public String edit(@RequestParam String nickname,
                       @RequestParam(required = false) String email,
                       @RequestParam(required = false) String bio,
                       HttpSession session) {
        Member member = (Member) session.getAttribute("loginMember");
        if (member == null) return "redirect:/member/login";
        memberService.updateProfile(member.getId(), nickname, email, bio);
        // 세션 갱신
        Member fresh = memberService.findById(member.getId()).orElseThrow();
        session.setAttribute("loginMember", fresh);
        return "redirect:/profile/me";
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 HttpSession session,
                                 Model model) {
        Member member = (Member) session.getAttribute("loginMember");
        if (member == null) return "redirect:/member/login";
        Member fresh = memberService.findById(member.getId()).orElseThrow();
        if (!memberService.checkPassword(currentPassword, fresh.getPassword())) {
            model.addAttribute("member", fresh);
            model.addAttribute("pwError", "현재 비밀번호가 올바르지 않습니다.");
            return "member/edit";
        }
        memberService.changePassword(member.getId(), newPassword);
        return "redirect:/profile/me";
    }

    @GetMapping("/{id}")
    public String viewProfile(@PathVariable Long id, Model model) {
        Member member = memberService.findById(id).orElseThrow();
        model.addAttribute("member", member);
        model.addAttribute("posts", postService.findByAuthor(member));
        return "member/profile";
    }
}
