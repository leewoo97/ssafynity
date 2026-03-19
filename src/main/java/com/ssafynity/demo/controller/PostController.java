package com.ssafynity.demo.controller;

import com.ssafynity.demo.domain.Member;
import com.ssafynity.demo.domain.Post;
import com.ssafynity.demo.service.CommentService;
import com.ssafynity.demo.service.PostLikeService;
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

@Controller
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {
    private final PostService postService;
    private final CommentService commentService;
    private final PostLikeService postLikeService;

    private static final String[] CATEGORIES = {"일반", "질문", "잡담", "정보", "공지"};

    @GetMapping
    public String list(@RequestParam(value = "keyword", required = false) String keyword,
                       @RequestParam(value = "category", required = false) String category,
                       @RequestParam(value = "page", defaultValue = "0") int page,
                       @RequestParam(value = "sort", defaultValue = "createdAt") String sort,
                       Model model) {
        int pageSize = 10;
        Sort pageSort = Sort.by(Sort.Direction.DESC, sort.equals("viewCount") || sort.equals("likeCount") ? sort : "createdAt");
        Pageable pageable = PageRequest.of(page, pageSize, pageSort);
        Page<Post> postPage;

        boolean hasKeyword = keyword != null && !keyword.isEmpty();
        boolean hasCategory = category != null && !category.isEmpty();

        if (hasCategory && hasKeyword) {
            postPage = postService.findByCategoryAndKeyword(category, keyword, pageable);
        } else if (hasCategory) {
            postPage = postService.findByCategory(category, pageable);
        } else if (hasKeyword) {
            postPage = postService.searchByTitlePaged(keyword, pageable);
        } else {
            postPage = postService.findAllPaged(pageable);
        }

        model.addAttribute("posts", postPage.getContent());
        model.addAttribute("page", page);
        model.addAttribute("totalPages", postPage.getTotalPages());
        model.addAttribute("keyword", keyword);
        model.addAttribute("category", category);
        model.addAttribute("sort", sort);
        model.addAttribute("categories", CATEGORIES);
        model.addAttribute("topViewed", postService.getTopViewedPosts());
        model.addAttribute("topLiked", postService.getTopLikedPosts());
        return "post/list";
    }

    @GetMapping("/new")
    public String createForm(HttpSession session, Model model) {
        if (session.getAttribute("loginMember") == null) {
            return "redirect:/member/login";
        }
        model.addAttribute("categories", CATEGORIES);
        return "post/form";
    }

    @PostMapping
    public String create(@RequestParam String title,
                         @RequestParam String content,
                         @RequestParam(value = "category", defaultValue = "일반") String category,
                         HttpSession session) {
        Member member = (Member) session.getAttribute("loginMember");
        if (member == null) return "redirect:/member/login";
        postService.createPost(title, content, category, member);
        return "redirect:/posts";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, HttpSession session, Model model) {
        Post post = postService.findByIdAndIncreaseView(id);
        Member loginMember = (Member) session.getAttribute("loginMember");
        model.addAttribute("post", post);
        model.addAttribute("comments", commentService.findByPost(post));
        model.addAttribute("liked", postLikeService.isLiked(loginMember, post));
        return "post/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, HttpSession session, Model model) {
        Member member = (Member) session.getAttribute("loginMember");
        Post post = postService.findById(id).orElseThrow();
        if (member == null || !post.getAuthor().getId().equals(member.getId())) {
            return "redirect:/posts/" + id;
        }
        model.addAttribute("post", post);
        model.addAttribute("categories", CATEGORIES);
        return "post/form";
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id,
                       @RequestParam String title,
                       @RequestParam String content,
                       @RequestParam(value = "category", defaultValue = "일반") String category,
                       HttpSession session) {
        Member member = (Member) session.getAttribute("loginMember");
        Post post = postService.findById(id).orElseThrow();
        if (member == null || !post.getAuthor().getId().equals(member.getId())) {
            return "redirect:/posts/" + id;
        }
        postService.updatePost(id, title, content, category);
        return "redirect:/posts/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, HttpSession session) {
        Member member = (Member) session.getAttribute("loginMember");
        Post post = postService.findById(id).orElseThrow();
        if (member == null || !post.getAuthor().getId().equals(member.getId())) {
            return "redirect:/posts/" + id;
        }
        postService.deletePost(id);
        return "redirect:/posts";
    }

    @PostMapping("/{id}/like")
    public String like(@PathVariable Long id, HttpSession session) {
        Member member = (Member) session.getAttribute("loginMember");
        if (member == null) return "redirect:/member/login";
        Post post = postService.findById(id).orElseThrow();
        postLikeService.toggleLike(member, post);
        return "redirect:/posts/" + id;
    }
}