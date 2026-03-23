package com.ssafynity.demo.controller;

import com.ssafynity.demo.domain.Member;
import com.ssafynity.demo.service.CommentService;
import com.ssafynity.demo.service.FriendshipService;
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
    private final FriendshipService friendshipService;

    @GetMapping("/me")
    public String myPage(@RequestParam(required = false) String tab, HttpSession session, Model model) {
        Member member = (Member) session.getAttribute("loginMember");
        if (member == null) return "redirect:/member/login";
        Member fresh = memberService.findById(member.getId()).orElseThrow();
        model.addAttribute("member", fresh);
        model.addAttribute("tab", tab);
        model.addAttribute("myPosts", postService.findByAuthor(fresh));
        model.addAttribute("myComments", commentService.findByAuthor(fresh));
        model.addAttribute("myFriends", friendshipService.getFriends(fresh));
        model.addAttribute("pendingRequests", friendshipService.getPendingReceived(fresh));
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
                       @RequestParam(required = false) String profileImageUrl,
                       @RequestParam(required = false) String realName,
                       @RequestParam(required = false) String campus,
                       @RequestParam(required = false) Integer cohort,
                       @RequestParam(required = false) Integer classCode,
                       HttpSession session) {
        Member member = (Member) session.getAttribute("loginMember");
        if (member == null) return "redirect:/member/login";
        memberService.updateProfile(member.getId(), nickname, email, bio,
                profileImageUrl, realName, campus, cohort, classCode);
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
    public String viewProfile(@PathVariable Long id, HttpSession session, Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        Member member = memberService.findById(id).orElseThrow();
        model.addAttribute("member", member);
        model.addAttribute("posts", postService.findByAuthor(member));
        model.addAttribute("loginMember", loginMember);
        if (loginMember != null && !loginMember.getId().equals(id)) {
            String friendStatus = friendshipService.getStatus(loginMember, member);
            model.addAttribute("friendStatus", friendStatus);  // null/PENDING/ACCEPTED/REJECTED
            model.addAttribute("isRequester", friendshipService.isRequester(loginMember, member));
            model.addAttribute("canSeeRealName", friendshipService.canSeeRealName(loginMember, member));
            model.addAttribute("isSameClass", memberService.isSameClass(loginMember, member));
            model.addAttribute("friendshipId", friendshipService.getFriendshipId(loginMember, member));
        }
        return "member/profile";
    }
}
