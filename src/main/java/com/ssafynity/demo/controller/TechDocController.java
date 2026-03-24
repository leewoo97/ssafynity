package com.ssafynity.demo.controller;

import com.ssafynity.demo.common.response.ApiResponse;
import com.ssafynity.demo.domain.Member;
import com.ssafynity.demo.domain.TechDoc;
import com.ssafynity.demo.dto.request.TechDocRequest;
import com.ssafynity.demo.dto.response.TechDocResponse;
import com.ssafynity.demo.security.CustomUserDetails;
import com.ssafynity.demo.service.MemberService;
import com.ssafynity.demo.service.TechDocService;
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
@RequestMapping("/api/docs")
@RequiredArgsConstructor
public class TechDocController {

    private final TechDocService techDocService;
    private final MemberService memberService;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<TechDoc> page = techDocService.searchDocs(keyword, category, pageable);
        List<TechDocResponse> content = page.getContent().stream().map(TechDocResponse::from).toList();
        Map<String, Object> result = Map.of(
                "content", content,
                "totalPages", page.getTotalPages(),
                "totalElements", page.getTotalElements(),
                "categories", List.of(techDocService.getCategories())
        );
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/pinned")
    public ResponseEntity<ApiResponse<List<TechDocResponse>>> pinned() {
        List<TechDocResponse> result = techDocService.getPinned().stream()
                .map(TechDocResponse::from).toList();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TechDocResponse>> detail(@PathVariable Long id) {
        TechDoc doc = techDocService.findByIdAndIncreaseView(id);
        return ResponseEntity.ok(ApiResponse.ok(TechDocResponse.from(doc)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<TechDocResponse>> create(
            @Valid @RequestBody TechDocRequest req,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Member member = memberService.getById(userDetails.getId());
        TechDoc doc = techDocService.create(req, member);
        return ResponseEntity.ok(ApiResponse.ok(TechDocResponse.from(doc)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> update(
            @PathVariable Long id,
            @Valid @RequestBody TechDocRequest req,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        techDocService.update(id, req, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        techDocService.delete(id, userDetails.getId(), userDetails.getRole());
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @PostMapping("/{id}/pin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> togglePin(@PathVariable Long id) {
        techDocService.togglePin(id);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
