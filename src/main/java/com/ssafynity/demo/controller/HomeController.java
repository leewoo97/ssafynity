package com.ssafynity.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * React SPA fallback: /api/** 이 아닌 모든 GET 요청을 index.html로 전달
 * (React Router가 클라이언트 라우팅 처리)
 */
@Controller
public class HomeController {

    @GetMapping(value = {"/{path:[^\\.]*}", "/{path:[^\\.]*}/**"})
    public String spa() {
        return "forward:/index.html";
    }
}
