package com.ssafynity.demo.controller;

import com.ssafynity.demo.common.response.ApiResponse;
import com.ssafynity.demo.domain.Comment;
import com.ssafynity.demo.domain.Member;
import com.ssafynity.demo.domain.Post;
import com.ssafynity.demo.dto.request.CommentRequest;
import com.ssafynity.demo.dto.response.CommentResponse;
import com.ssafynity.demo.security.CustomUserDetails;
import com.ssafynity.demo.service.CommentService;
import com.ssafynity.demo.service.MemberService;
import com.ssafynity.demo.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final PostService postService;
    private final MemberService memberService;

    @GetMapping("/post/{postId}")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> listByPost(@PathVariable Long postId) {
        Post post = postService.getById(postId);
        List<CommentResponse> result = commentService.findByPost(post).stream()
                .map(CommentResponse::from).toList();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PostMapping("/post/{postId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CommentResponse>> create(
            @PathVariable Long postId,
            @Valid @RequestBody CommentRequest req,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Post post = postService.getById(postId);
        Member member = memberService.getById(userDetails.getId());
        Comment comment = commentService.addComment(req.getContent(), member, post);
        return ResponseEntity.ok(ApiResponse.ok(CommentResponse.from(comment)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        commentService.deleteComment(id, userDetails.getId(), userDetails.getRole());
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
