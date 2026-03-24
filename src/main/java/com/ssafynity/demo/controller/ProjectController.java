package com.ssafynity.demo.controller;

import com.ssafynity.demo.common.response.ApiResponse;
import com.ssafynity.demo.domain.Member;
import com.ssafynity.demo.domain.Project;
import com.ssafynity.demo.dto.request.ProjectRequest;
import com.ssafynity.demo.dto.response.ProjectResponse;
import com.ssafynity.demo.security.CustomUserDetails;
import com.ssafynity.demo.service.MemberService;
import com.ssafynity.demo.service.ProjectService;
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
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final MemberService memberService;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> list(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Project> page = (keyword != null)
                ? projectService.searchByKeyword(keyword, pageable)
                : projectService.findAllPaged(pageable);
        List<ProjectResponse> content = page.getContent().stream().map(ProjectResponse::from).toList();
        Map<String, Object> result = Map.of(
                "content", content,
                "totalPages", page.getTotalPages(),
                "totalElements", page.getTotalElements()
        );
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> detail(@PathVariable Long id) {
        Project project = projectService.findByIdAndIncreaseView(id);
        return ResponseEntity.ok(ApiResponse.ok(ProjectResponse.from(project)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ProjectResponse>> create(
            @Valid @RequestBody ProjectRequest req,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Member member = memberService.getById(userDetails.getId());
        Project project = projectService.create(req, member);
        return ResponseEntity.ok(ApiResponse.ok(ProjectResponse.from(project)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> update(
            @PathVariable Long id,
            @Valid @RequestBody ProjectRequest req,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        projectService.update(id, req, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        projectService.delete(id, userDetails.getId(), userDetails.getRole());
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @PostMapping("/{id}/like")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> like(@PathVariable Long id) {
        projectService.like(id);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
