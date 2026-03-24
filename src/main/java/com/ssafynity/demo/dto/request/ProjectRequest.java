package com.ssafynity.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProjectRequest {

    @NotBlank(message = "제목을 입력해 주세요.")
    private String title;

    @NotBlank(message = "설명을 입력해 주세요.")
    private String description;

    private String techStack;
    private String githubUrl;
    private String demoUrl;
    private String thumbnailUrl;
    private int teamSize = 1;
    private String status = "COMPLETED";
}
