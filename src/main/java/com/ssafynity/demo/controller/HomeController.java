package com.ssafynity.demo.controller;

import com.ssafynity.demo.common.response.ApiResponse;
import com.ssafynity.demo.dto.response.*;
import com.ssafynity.demo.repository.MemberRepository;
import com.ssafynity.demo.repository.PostRepository;
import com.ssafynity.demo.repository.TechDocRepository;
import com.ssafynity.demo.repository.TechVideoRepository;
import com.ssafynity.demo.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final TechDocRepository techDocRepository;
    private final TechVideoRepository techVideoRepository;
    private final PostService postService;
    private final TechDocService techDocService;
    private final TechVideoService techVideoService;
    private final EventService eventService;
    private final ProjectService projectService;

    @GetMapping("/api/home")
    @ResponseBody
    public ResponseEntity<ApiResponse<Map<String, Object>>> home() {
        Map<String, Object> data = Map.of(
            "memberCount", memberRepository.count(),
            "postCount", postRepository.count(),
            "docCount", techDocRepository.count(),
            "videoCount", techVideoRepository.count(),
            "hotPosts", postService.getTopViewedPosts().stream().map(PostResponse::from).toList(),
            "topLiked", postService.getTopLikedPosts().stream().map(PostResponse::from).toList(),
            "latestDocs", techDocService.getLatest().stream().map(TechDocResponse::from).toList(),
            "pinnedDocs", techDocService.getPinned().stream().map(TechDocResponse::from).toList(),
            "latestVideos", techVideoService.getLatest().stream().map(TechVideoResponse::from).toList(),
            "upcomingEvents", eventService.findUpcomingTop4().stream().map(EventResponse::from).toList()
        );
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    /** React SPA fallback: /api/** 이 아닌 모든 GET 요청을 index.html로 전달 */
    @GetMapping(value = {"/{path:[^\\.]*}", "/{path:[^\\.]*}/**"})
    public String spa() {
        return "forward:/index.html";
    }
}
