package com.ssafynity.demo.controller;

import com.ssafynity.demo.domain.Member;
import com.ssafynity.demo.domain.Post;
import com.ssafynity.demo.service.CommentService;
import com.ssafynity.demo.service.NotificationService;
import com.ssafynity.demo.service.PostService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/comments")
public class CommentController {
    private final CommentService commentService;
    private final PostService postService;
    private final NotificationService notificationService;

    @PostMapping("/add")
    public String addComment(@RequestParam Long postId,
                             @RequestParam String content,
                             HttpSession session) {
        Member member = (Member) session.getAttribute("loginMember");
        if (member == null) return "redirect:/member/login";
        Post post = postService.findById(postId).orElseThrow();
        commentService.addComment(content, member, post);
        // 게시글 작성자에게 알림 (본인 댓글 제외)
        if (!post.getAuthor().getId().equals(member.getId())) {
            notificationService.send(
                post.getAuthor(),
                "'" + post.getTitle() + "' 글에 " + member.getNickname() + "님이 댓글을 달았습니다.",
                "/posts/" + postId
            );
        }
        return "redirect:/posts/" + postId;
    }

    @PostMapping("/{id}/delete")
    public String deleteComment(@PathVariable Long id,
                                @RequestParam Long postId,
                                HttpSession session) {
        Member member = (Member) session.getAttribute("loginMember");
        if (member == null) return "redirect:/member/login";
        commentService.deleteComment(id, member);
        return "redirect:/posts/" + postId;
    }
}
