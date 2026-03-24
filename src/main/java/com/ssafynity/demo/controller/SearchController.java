package com.ssafynity.demo.controller;

import com.ssafynity.demo.common.response.ApiResponse;
import com.ssafynity.demo.dto.response.PostResponse;
import com.ssafynity.demo.dto.response.ProjectResponse;
import com.ssafynity.demo.dto.response.TechDocResponse;
import com.ssafynity.demo.dto.response.MemberResponse;
import com.ssafynity.demo.service.MemberService;
import com.ssafynity.demo.service.PostService;
import com.ssafynity.demo.service.ProjectService;
import com.ssafynity.demo.service.TechDocService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final PostService postService;
    private final ProjectService projectService;
    private final TechDocService techDocService;
    private final MemberService memberService;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> search(
            @RequestParam String q,
            @PageableDefault(size = 10) Pageable pageable) {
        List<PostResponse> posts = postService.searchByTitle(q).stream()
                .map(PostResponse::from).toList();
        List<ProjectResponse> projects = projectService.searchByKeyword(q, pageable).getContent().stream()
                .map(ProjectResponse::from).toList();
        List<TechDocResponse> docs = techDocService.searchDocs(q, null, pageable).getContent().stream()
                .map(TechDocResponse::from).toList();
        List<MemberResponse> members = memberService.findAll().stream()
                .filter(m -> m.getNickname().contains(q) || m.getUsername().contains(q))
                .map(MemberResponse::of).toList();

        Map<String, Object> result = Map.of(
                "query", q,
                "posts", posts,
                "projects", projects,
                "docs", docs,
                "members", members
        );
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
