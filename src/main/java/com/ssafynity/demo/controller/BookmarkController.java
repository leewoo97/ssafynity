package com.ssafynity.demo.controller;

import com.ssafynity.demo.domain.Bookmark;
import com.ssafynity.demo.domain.Member;
import com.ssafynity.demo.service.BookmarkService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/bookmarks")
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @GetMapping
    public String list(@RequestParam(required = false) String type,
                       HttpSession session, Model model) {
        Member member = (Member) session.getAttribute("loginMember");
        if (member == null) return "redirect:/member/login";
        List<Bookmark> all = bookmarkService.findByMember(member);
        List<Bookmark> bookmarks = (type != null && !type.isBlank())
                ? all.stream().filter(b -> type.equals(b.getTargetType())).toList()
                : all;
        model.addAttribute("bookmarks", bookmarks);
        model.addAttribute("activeFilter", (type != null && !type.isBlank()) ? type : null);
        return "bookmark/list";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, HttpSession session) {
        Member member = (Member) session.getAttribute("loginMember");
        if (member == null) return "redirect:/member/login";
        bookmarkService.delete(id);
        return "redirect:/bookmarks";
    }
}
