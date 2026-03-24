package com.ssafynity.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TechVideoRequest {

    @NotBlank(message = "제목을 입력해 주세요.")
    private String title;

    private String description;

    @NotBlank(message = "YouTube URL 또는 ID를 입력해 주세요.")
    private String youtubeUrl;

    private String duration;
    private String category = "기타";
    private String tags;
}
