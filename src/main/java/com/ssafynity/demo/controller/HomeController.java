package com.ssafynity.demo.controller;

import com.ssafynity.demo.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {
    private final PostService postService;
    private final TechDocService techDocService;
    private final TechVideoService techVideoService;
    private final EventService eventService;
    private final ProjectService projectService;
    private final MemberService memberService;

    @GetMapping("/")
    public String home(Model model) {
        // 게시판
        model.addAttribute("topViewed", postService.getTopViewedPosts());
        model.addAttribute("topLiked", postService.getTopLikedPosts());
        model.addAttribute("hotPosts", postService.getHotPosts(7, 5));

        // 문서·영상
        model.addAttribute("latestDocs", techDocService.getLatest());
        model.addAttribute("latestVideos", techVideoService.getLatest());
        model.addAttribute("pinnedDocs", techDocService.getPinned());

        // 이벤트·프로젝트
        model.addAttribute("upcomingEvents", eventService.findUpcomingTop4());
        model.addAttribute("featuredProjects", projectService.getTopLiked());

        // 사이트 통계
        model.addAttribute("memberCount", memberService.findAll().size());
        model.addAttribute("postCount", postService.findAll().size());
        model.addAttribute("docCount", techDocService.findAll().size());
        model.addAttribute("videoCount", techVideoService.findAll().size());
        return "index";
    }
}
