package com.ssafynity.demo.controller;

import com.ssafynity.demo.domain.Friendship;
import com.ssafynity.demo.domain.Member;
import com.ssafynity.demo.service.FriendshipService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 친구 요청/수락/거절/목록
 * POST /friends/request/{targetId}  → 친구 요청 전송
 * POST /friends/accept/{id}         → 수락
 * POST /friends/reject/{id}         → 거절
 * POST /friends/unfriend/{targetId} → 친구 끊기
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/friends")
public class FriendController {

    private final FriendshipService friendshipService;

    @PostMapping("/request/{targetId}")
    public String sendRequest(@PathVariable Long targetId,
                              @RequestParam(defaultValue = "") String redirect,
                              HttpSession session) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/member/login";
        friendshipService.sendRequest(loginMember, targetId);
        return redirect.isBlank() ? "redirect:/profile/" + targetId : "redirect:" + redirect;
    }

    @PostMapping("/accept/{friendshipId}")
    public String accept(@PathVariable Long friendshipId, HttpSession session) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/member/login";
        friendshipService.accept(friendshipId, loginMember);
        return "redirect:/profile/me?tab=friends";
    }

    @PostMapping("/reject/{friendshipId}")
    public String reject(@PathVariable Long friendshipId, HttpSession session) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/member/login";
        friendshipService.reject(friendshipId, loginMember);
        return "redirect:/profile/me?tab=friends";
    }

    @PostMapping("/unfriend/{targetId}")
    public String unfriend(@PathVariable Long targetId, HttpSession session) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/member/login";
        friendshipService.unfriend(loginMember, targetId);
        return "redirect:/profile/me?tab=friends";
    }
}
