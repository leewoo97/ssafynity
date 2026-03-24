package com.ssafynity.demo.controller;

import com.ssafynity.demo.common.response.ApiResponse;
import com.ssafynity.demo.domain.Member;
import com.ssafynity.demo.domain.TechVideo;
import com.ssafynity.demo.dto.request.TechVideoRequest;
import com.ssafynity.demo.dto.response.TechVideoResponse;
import com.ssafynity.demo.security.CustomUserDetails;
import com.ssafynity.demo.service.MemberService;
import com.ssafynity.demo.service.TechVideoService;
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
@RequestMapping("/api/videos")
@RequiredArgsConstructor
public class TechVideoController {

    private final TechVideoService techVideoService;
    private final MemberService memberService;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<TechVideo> page;
        if (keyword != null) {
            page = techVideoService.searchByTitle(keyword, pageable);
        } else if (category != null) {
            page = techVideoService.findByCategory(category, pageable);
        } else {
            page = techVideoService.findAllPaged(pageable);
        }
        List<TechVideoResponse> content = page.getContent().stream().map(TechVideoResponse::from).toList();
        Map<String, Object> result = Map.of(
                "content", content,
                "totalPages", page.getTotalPages(),
                "totalElements", page.getTotalElements(),
                "categories", List.of(techVideoService.getCategories())
        );
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TechVideoResponse>> detail(@PathVariable Long id) {
        TechVideo video = techVideoService.findByIdAndIncreaseView(id);
        return ResponseEntity.ok(ApiResponse.ok(TechVideoResponse.from(video)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<TechVideoResponse>> create(
            @Valid @RequestBody TechVideoRequest req,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Member member = memberService.getById(userDetails.getId());
        TechVideo video = techVideoService.create(req, member);
        return ResponseEntity.ok(ApiResponse.ok(TechVideoResponse.from(video)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> update(
            @PathVariable Long id,
            @Valid @RequestBody TechVideoRequest req,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        techVideoService.update(id, req, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        techVideoService.delete(id, userDetails.getId(), userDetails.getRole());
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @PostMapping("/{id}/pin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> togglePin(@PathVariable Long id) {
        techVideoService.togglePin(id);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
