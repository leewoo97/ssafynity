package com.ssafynity.demo.controller;

import com.ssafynity.demo.domain.Member;
import com.ssafynity.demo.domain.TechVideo;
import com.ssafynity.demo.service.BookmarkService;
import com.ssafynity.demo.service.TechVideoService;
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
@RequestMapping("/videos")
public class TechVideoController {

    private final TechVideoService techVideoService;
    private final BookmarkService bookmarkService;

    @GetMapping
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) String category,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        Pageable pageable = PageRequest.of(page, 12);
        Page<TechVideo> videoPage;
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        boolean hasCat     = category != null && !category.isBlank();
        if (hasKeyword)    videoPage = techVideoService.searchByTitle(keyword, pageable);
        else if (hasCat)   videoPage = techVideoService.findByCategory(category, pageable);
        else               videoPage = techVideoService.findAllPaged(pageable);
        model.addAttribute("videos", videoPage.getContent());
        model.addAttribute("totalPages", videoPage.getTotalPages());
        model.addAttribute("currentPage", page);
        model.addAttribute("keyword", keyword);
        model.addAttribute("category", category);
        model.addAttribute("categories", techVideoService.getCategories());
        model.addAttribute("pinnedVideos", techVideoService.getPinned());
        return "videos/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, HttpSession session) {
        TechVideo video = techVideoService.findByIdAndIncreaseView(id);
        Member loginMember = (Member) session.getAttribute("loginMember");
        model.addAttribute("video", video);
        model.addAttribute("isBookmarked", bookmarkService.isBookmarked(loginMember, "VIDEO", id));
        model.addAttribute("relatedVideos", techVideoService.getTopViewed());
        return "videos/detail";
    }

    @GetMapping("/new")
    public String createForm(HttpSession session, Model model) {
        if (session.getAttribute("loginMember") == null) return "redirect:/member/login";
        model.addAttribute("video", new TechVideo());
        model.addAttribute("categories", techVideoService.getCategories());
        return "videos/form";
    }

    @PostMapping
    public String create(@RequestParam String title,
                         @RequestParam(required = false) String description,
                         @RequestParam String youtubeUrl,
                         @RequestParam(required = false) String duration,
                         @RequestParam(required = false) String category,
                         @RequestParam(required = false) String tags,
                         HttpSession session) {
        Member member = (Member) session.getAttribute("loginMember");
        if (member == null) return "redirect:/member/login";
        TechVideo video = techVideoService.create(title, description, youtubeUrl, duration, category, tags, member);
        return "redirect:/videos/" + video.getId();
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, HttpSession session, Model model) {
        Member member = (Member) session.getAttribute("loginMember");
        TechVideo video = techVideoService.findById(id).orElseThrow();
        if (member == null || !video.getAuthor().getId().equals(member.getId())) {
            return "redirect:/videos/" + id;
        }
        model.addAttribute("video", video);
        model.addAttribute("categories", techVideoService.getCategories());
        return "videos/form";
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id,
                       @RequestParam String title,
                       @RequestParam(required = false) String description,
                       @RequestParam String youtubeUrl,
                       @RequestParam(required = false) String duration,
                       @RequestParam(required = false) String category,
                       @RequestParam(required = false) String tags,
                       HttpSession session) {
        Member member = (Member) session.getAttribute("loginMember");
        TechVideo video = techVideoService.findById(id).orElseThrow();
        if (member == null || !video.getAuthor().getId().equals(member.getId())) {
            return "redirect:/videos/" + id;
        }
        techVideoService.update(id, title, description, youtubeUrl, duration, category, tags);
        return "redirect:/videos/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, HttpSession session) {
        Member member = (Member) session.getAttribute("loginMember");
        TechVideo video = techVideoService.findById(id).orElseThrow();
        boolean isOwner = member != null && video.getAuthor().getId().equals(member.getId());
        boolean isAdmin = member != null && "ADMIN".equals(member.getRole());
        if (!isOwner && !isAdmin) return "redirect:/videos/" + id;
        techVideoService.delete(id);
        return "redirect:/videos";
    }

    @PostMapping("/{id}/pin")
    public String togglePin(@PathVariable Long id, HttpSession session) {
        Member member = (Member) session.getAttribute("loginMember");
        if (member == null || !"ADMIN".equals(member.getRole())) return "redirect:/videos/" + id;
        techVideoService.togglePin(id);
        return "redirect:/videos/" + id;
    }

    @PostMapping("/{id}/bookmark")
    public String bookmark(@PathVariable Long id, HttpSession session) {
        Member member = (Member) session.getAttribute("loginMember");
        if (member == null) return "redirect:/member/login";
        TechVideo video = techVideoService.findById(id).orElseThrow();
        bookmarkService.toggle(member, "VIDEO", id, video.getTitle());
        return "redirect:/videos/" + id;
    }
}
