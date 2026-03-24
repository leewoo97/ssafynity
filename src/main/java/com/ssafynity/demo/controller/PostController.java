package com.ssafynity.demo.controller;

import com.ssafynity.demo.common.response.ApiResponse;
import com.ssafynity.demo.domain.Member;
import com.ssafynity.demo.domain.Post;
import com.ssafynity.demo.dto.request.PostRequest;
import com.ssafynity.demo.dto.response.PostResponse;
import com.ssafynity.demo.security.CustomUserDetails;
import com.ssafynity.demo.service.MemberService;
import com.ssafynity.demo.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final MemberService memberService;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Post> page;
        if (category != null && keyword != null) {
            page = postService.findByCategoryAndKeyword(category, keyword, pageable);
        } else if (category != null) {
            page = postService.findByCategory(category, pageable);
        } else if (keyword != null) {
            page = postService.searchByTitlePaged(keyword, pageable);
        } else {
            page = postService.findAllPaged(pageable);
        }
        List<PostResponse> content = page.getContent().stream().map(PostResponse::from).toList();
        Map<String, Object> result = Map.of(
                "content", content,
                "totalPages", page.getTotalPages(),
                "totalElements", page.getTotalElements(),
                "page", page.getNumber(),
                "size", page.getSize()
        );
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/hot")
    public ResponseEntity<ApiResponse<List<PostResponse>>> hot() {
        List<PostResponse> result = postService.getTopViewedPosts().stream()
                .map(PostResponse::from).toList();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PostResponse>> detail(@PathVariable Long id) {
        Post post = postService.findByIdAndIncreaseView(id);
        return ResponseEntity.ok(ApiResponse.ok(PostResponse.from(post)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PostResponse>> create(
            @Valid @RequestBody PostRequest req,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Member member = memberService.getById(userDetails.getId());
        Post post = postService.createPost(req, member);
        return ResponseEntity.ok(ApiResponse.ok(PostResponse.from(post)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> update(
            @PathVariable Long id,
            @Valid @RequestBody PostRequest req,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        postService.updatePost(id, req, userDetails.getId(), userDetails.getRole());
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        postService.deletePost(id, userDetails.getId(), userDetails.getRole());
        return ResponseEntity.ok(ApiResponse.ok());
    }

    /** 캠퍼스 게시판 */
    @GetMapping("/campus/{campus}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> campus(
            @PathVariable String campus,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Post> page = (keyword != null)
                ? postService.findByCampusAndKeyword(campus, keyword, pageable)
                : postService.findByCampusPaged(campus, pageable);
        List<PostResponse> content = page.getContent().stream().map(PostResponse::from).toList();
        Map<String, Object> result = Map.of(
                "content", content,
                "totalPages", page.getTotalPages(),
                "totalElements", page.getTotalElements()
        );
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PostMapping("/campus")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PostResponse>> createCampus(
            @Valid @RequestBody PostRequest req,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Member member = memberService.getById(userDetails.getId());
        Post post = postService.createCampusPost(req, member);
        return ResponseEntity.ok(ApiResponse.ok(PostResponse.from(post)));
    }
}
