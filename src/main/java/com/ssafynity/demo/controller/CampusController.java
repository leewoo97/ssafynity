package com.ssafynity.demo.controller;

import com.ssafynity.demo.common.response.ApiResponse;
import com.ssafynity.demo.dto.response.MemberResponse;
import com.ssafynity.demo.dto.response.PostResponse;
import com.ssafynity.demo.service.MemberService;
import com.ssafynity.demo.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/campus")
@RequiredArgsConstructor
public class CampusController {

    private final MemberService memberService;
    private final PostService postService;

    @GetMapping("/{campus}/members")
    public ResponseEntity<ApiResponse<List<MemberResponse>>> members(@PathVariable String campus) {
        List<MemberResponse> result = memberService.findByCampus(campus).stream()
                .map(MemberResponse::of).toList();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/{campus}/posts")
    public ResponseEntity<ApiResponse<List<PostResponse>>> posts(@PathVariable String campus) {
        List<PostResponse> result = postService.getRecentCampusPosts(campus).stream()
                .map(PostResponse::from).toList();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
