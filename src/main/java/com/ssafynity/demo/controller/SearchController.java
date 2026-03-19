package com.ssafynity.demo.controller;

import com.ssafynity.demo.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/search")
public class SearchController {

    private final PostService postService;
    private final TechDocService techDocService;
    private final TechVideoService techVideoService;
    private final ProjectService projectService;

    @GetMapping
    public String search(@RequestParam(required = false) String q, Model model) {
        if (q == null || q.isBlank()) {
            model.addAttribute("q", "");
            return "search/results";
        }
        Pageable pageable = PageRequest.of(0, 10);

        // QueryDSL 고급 검색 (제목+본문 모두 검색)
        var posts    = postService.searchAdvanced(q, null, "createdAt", pageable).getContent();
        var docs     = techDocService.searchDocs(q, null, PageRequest.of(0, 6)).getContent();
        var videos   = techVideoService.searchByTitle(q, PageRequest.of(0, 6)).getContent();
        var projects = projectService.searchByKeyword(q, PageRequest.of(0, 4)).getContent();
        model.addAttribute("posts", posts);
        model.addAttribute("docs", docs);
        model.addAttribute("videos", videos);
        model.addAttribute("projects", projects);
        model.addAttribute("totalCount", posts.size() + docs.size() + videos.size() + projects.size());
        model.addAttribute("q", q);
        return "search/results";
    }
}
