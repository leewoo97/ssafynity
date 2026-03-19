package com.ssafynity.demo.controller;

import com.ssafynity.demo.domain.Member;
import com.ssafynity.demo.service.NotificationService;
import com.ssafynity.demo.service.ReportService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/report")
public class ReportController {

    private final ReportService reportService;
    private final NotificationService notificationService;

    @PostMapping
    public String report(@RequestParam String targetType,
                         @RequestParam Long targetId,
                         @RequestParam String reason,
                         HttpSession session) {
        Member member = (Member) session.getAttribute("loginMember");
        if (member == null) return "redirect:/member/login";
        reportService.report(member, targetType, targetId, reason);
        String redirectUrl = targetType.equals("POST") ? "/posts/" + targetId : "/posts";
        return "redirect:" + redirectUrl + "?reported=1";
    }

    @GetMapping("/form")
    public String form(@RequestParam String targetType,
                       @RequestParam Long targetId,
                       HttpSession session,
                       Model model) {
        if (session.getAttribute("loginMember") == null) return "redirect:/member/login";
        model.addAttribute("targetType", targetType);
        model.addAttribute("targetId", targetId);
        return "report/form";
    }
}
