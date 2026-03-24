package com.ssafynity.demo.controller;

import com.ssafynity.demo.common.response.ApiResponse;
import com.ssafynity.demo.domain.Member;
import com.ssafynity.demo.domain.Report;
import com.ssafynity.demo.dto.response.MemberResponse;
import com.ssafynity.demo.service.BookmarkService;
import com.ssafynity.demo.service.CommentService;
import com.ssafynity.demo.service.MemberService;
import com.ssafynity.demo.service.PostService;
import com.ssafynity.demo.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final MemberService memberService;
    private final PostService postService;
    private final CommentService commentService;
    private final ReportService reportService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> dashboard() {
        Map<String, Object> stats = Map.of(
                "totalMembers", memberService.findAll().size(),
                "totalPosts", postService.findAll().size(),
                "totalComments", commentService.countAll(),
                "pendingReports", reportService.findPending().size()
        );
        return ResponseEntity.ok(ApiResponse.ok(stats));
    }

    @GetMapping("/members")
    public ResponseEntity<ApiResponse<List<MemberResponse>>> members() {
        List<MemberResponse> result = memberService.findAll().stream()
                .map(MemberResponse::ofWithRealName).toList();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @DeleteMapping("/members/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMember(@PathVariable Long id) {
        memberService.deleteMember(id);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @GetMapping("/reports")
    public ResponseEntity<ApiResponse<List<Report>>> reports() {
        return ResponseEntity.ok(ApiResponse.ok(reportService.findPending()));
    }

    @PostMapping("/reports/{id}/resolve")
    public ResponseEntity<ApiResponse<Void>> resolveReport(@PathVariable Long id) {
        reportService.resolve(id);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
