package com.ssafynity.demo.controller;

import com.ssafynity.demo.domain.Member;
import com.ssafynity.demo.domain.Project;
import com.ssafynity.demo.service.BookmarkService;
import com.ssafynity.demo.service.ProjectService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService projectService;
    private final BookmarkService bookmarkService;

    @GetMapping
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "latest") String sort,
                       Model model) {
        Sort pageSort = sort.equals("popular")
                ? Sort.by(Sort.Direction.DESC, "likeCount")
                : Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page, 9, pageSort);
        Page<Project> projectPage = (keyword != null && !keyword.isBlank())
                ? projectService.searchByKeyword(keyword, pageable)
                : projectService.findAllPaged(pageable);
        model.addAttribute("projects", projectPage.getContent());
        model.addAttribute("totalPages", projectPage.getTotalPages());
        model.addAttribute("currentPage", page);
        model.addAttribute("keyword", keyword);
        model.addAttribute("sort", sort);
        model.addAttribute("featuredProjects", projectService.getTopLiked());
        return "projects/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, HttpSession session) {
        Project project = projectService.findByIdAndIncreaseView(id);
        Member loginMember = (Member) session.getAttribute("loginMember");
        model.addAttribute("project", project);
        model.addAttribute("isBookmarked", bookmarkService.isBookmarked(loginMember, "PROJECT", id));
        model.addAttribute("relatedProjects", projectService.getLatest());
        return "projects/detail";
    }

    @GetMapping("/new")
    public String createForm(HttpSession session, Model model) {
        if (session.getAttribute("loginMember") == null) return "redirect:/member/login";
        model.addAttribute("project", new Project());
        return "projects/form";
    }

    @PostMapping
    public String create(@RequestParam String title,
                         @RequestParam String description,
                         @RequestParam(required = false) String techStack,
                         @RequestParam(required = false) String githubUrl,
                         @RequestParam(required = false) String demoUrl,
                         @RequestParam(required = false) String thumbnailUrl,
                         @RequestParam(defaultValue = "1") int teamSize,
                         @RequestParam(defaultValue = "COMPLETED") String status,
                         HttpSession session) {
        Member member = (Member) session.getAttribute("loginMember");
        if (member == null) return "redirect:/member/login";
        Project project = projectService.create(title, description, techStack, githubUrl,
                demoUrl, thumbnailUrl, teamSize, status, member);
        return "redirect:/projects/" + project.getId();
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, HttpSession session, Model model) {
        Member member = (Member) session.getAttribute("loginMember");
        Project project = projectService.findById(id).orElseThrow();
        if (member == null || !project.getAuthor().getId().equals(member.getId())) {
            return "redirect:/projects/" + id;
        }
        model.addAttribute("project", project);
        return "projects/form";
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id,
                       @RequestParam String title,
                       @RequestParam String description,
                       @RequestParam(required = false) String techStack,
                       @RequestParam(required = false) String githubUrl,
                       @RequestParam(required = false) String demoUrl,
                       @RequestParam(required = false) String thumbnailUrl,
                       @RequestParam(defaultValue = "1") int teamSize,
                       @RequestParam(defaultValue = "COMPLETED") String status,
                       HttpSession session) {
        Member member = (Member) session.getAttribute("loginMember");
        Project project = projectService.findById(id).orElseThrow();
        if (member == null || !project.getAuthor().getId().equals(member.getId())) {
            return "redirect:/projects/" + id;
        }
        projectService.update(id, title, description, techStack, githubUrl, demoUrl,
                thumbnailUrl, teamSize, status);
        return "redirect:/projects/" + id;
    }

    @PostMapping("/{id}/like")
    public String like(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("loginMember") == null) return "redirect:/member/login";
        projectService.like(id);
        return "redirect:/projects/" + id;
    }

    @PostMapping("/{id}/bookmark")
    public String bookmark(@PathVariable Long id, HttpSession session) {
        Member member = (Member) session.getAttribute("loginMember");
        if (member == null) return "redirect:/member/login";
        Project project = projectService.findById(id).orElseThrow();
        bookmarkService.toggle(member, "PROJECT", id, project.getTitle());
        return "redirect:/projects/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, HttpSession session) {
        Member member = (Member) session.getAttribute("loginMember");
        Project project = projectService.findById(id).orElseThrow();
        boolean isOwner = member != null && project.getAuthor().getId().equals(member.getId());
        boolean isAdmin = member != null && "ADMIN".equals(member.getRole());
        if (!isOwner && !isAdmin) return "redirect:/projects/" + id;
        projectService.delete(id);
        return "redirect:/projects";
    }
}
