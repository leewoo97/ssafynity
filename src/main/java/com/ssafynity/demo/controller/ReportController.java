package com.ssafynity.demo.controller;

import com.ssafynity.demo.common.response.ApiResponse;
import com.ssafynity.demo.domain.Member;
import com.ssafynity.demo.dto.request.ReportRequest;
import com.ssafynity.demo.security.CustomUserDetails;
import com.ssafynity.demo.service.MemberService;
import com.ssafynity.demo.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ReportController {

    private final ReportService reportService;
    private final MemberService memberService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> report(
            @Valid @RequestBody ReportRequest req,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Member reporter = memberService.getById(userDetails.getId());
        reportService.report(reporter, req.getTargetType(), req.getTargetId(), req.getReason());
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
