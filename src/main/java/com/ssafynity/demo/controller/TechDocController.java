package com.ssafynity.demo.controller;

import com.ssafynity.demo.domain.Member;
import com.ssafynity.demo.domain.TechDoc;
import com.ssafynity.demo.service.BookmarkService;
import com.ssafynity.demo.service.TechDocService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/docs")
public class TechDocController {

    private final TechDocService techDocService;
    private final BookmarkService bookmarkService;

    @GetMapping
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) String category,
                       @RequestParam(defaultValue = "0") int page,
                       Model model, HttpSession session) {
        Pageable pageable = PageRequest.of(page, 12);
        Page<TechDoc> docPage = techDocService.searchDocs(keyword, category, pageable);
        model.addAttribute("docs", docPage.getContent());
        model.addAttribute("totalPages", docPage.getTotalPages());
        model.addAttribute("currentPage", page);
        model.addAttribute("keyword", keyword);
        model.addAttribute("category", category);
        model.addAttribute("categories", techDocService.getCategories());
        model.addAttribute("pinnedDocs", techDocService.getPinned());
        return "docs/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, HttpSession session) {
        TechDoc doc = techDocService.findByIdAndIncreaseView(id);
        Member loginMember = (Member) session.getAttribute("loginMember");
        model.addAttribute("doc", doc);
        model.addAttribute("isBookmarked", bookmarkService.isBookmarked(loginMember, "DOC", id));
        model.addAttribute("relatedDocs", techDocService.getTopViewed());
        return "docs/detail";
    }

    @GetMapping("/new")
    public String createForm(HttpSession session, Model model) {
        if (session.getAttribute("loginMember") == null) return "redirect:/member/login";
        model.addAttribute("doc", new TechDoc());
        model.addAttribute("categories", techDocService.getCategories());
        return "docs/form";
    }

    @PostMapping
    public String create(@RequestParam String title,
                         @RequestParam String content,
                         @RequestParam(required = false) String category,
                         @RequestParam(required = false) String tags,
                         HttpSession session) {
        Member member = (Member) session.getAttribute("loginMember");
        if (member == null) return "redirect:/member/login";
        TechDoc doc = techDocService.create(title, content, category, tags, member);
        return "redirect:/docs/" + doc.getId();
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, HttpSession session, Model model) {
        Member member = (Member) session.getAttribute("loginMember");
        TechDoc doc = techDocService.findById(id).orElseThrow();
        if (member == null || !doc.getAuthor().getId().equals(member.getId())) {
            return "redirect:/docs/" + id;
        }
        model.addAttribute("doc", doc);
        model.addAttribute("categories", techDocService.getCategories());
        return "docs/form";
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id,
                       @RequestParam String title,
                       @RequestParam String content,
                       @RequestParam(required = false) String category,
                       @RequestParam(required = false) String tags,
                       HttpSession session) {
        Member member = (Member) session.getAttribute("loginMember");
        TechDoc doc = techDocService.findById(id).orElseThrow();
        if (member == null || !doc.getAuthor().getId().equals(member.getId())) {
            return "redirect:/docs/" + id;
        }
        techDocService.update(id, title, content, category, tags);
        return "redirect:/docs/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, HttpSession session) {
        Member member = (Member) session.getAttribute("loginMember");
        TechDoc doc = techDocService.findById(id).orElseThrow();
        boolean isOwner = member != null && doc.getAuthor().getId().equals(member.getId());
        boolean isAdmin = member != null && "ADMIN".equals(member.getRole());
        if (!isOwner && !isAdmin) return "redirect:/docs/" + id;
        techDocService.delete(id);
        return "redirect:/docs";
    }

    @PostMapping("/{id}/pin")
    public String togglePin(@PathVariable Long id, HttpSession session) {
        Member member = (Member) session.getAttribute("loginMember");
        if (member == null || !"ADMIN".equals(member.getRole())) return "redirect:/docs/" + id;
        techDocService.togglePin(id);
        return "redirect:/docs/" + id;
    }

    @PostMapping("/{id}/bookmark")
    public String bookmark(@PathVariable Long id, HttpSession session) {
        Member member = (Member) session.getAttribute("loginMember");
        if (member == null) return "redirect:/member/login";
        TechDoc doc = techDocService.findById(id).orElseThrow();
        bookmarkService.toggle(member, "DOC", id, doc.getTitle());
        return "redirect:/docs/" + id;
    }
}
