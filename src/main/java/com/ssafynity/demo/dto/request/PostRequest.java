package com.ssafynity.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PostRequest {

    @NotBlank(message = "제목을 입력해 주세요.")
    private String title;

    @NotBlank(message = "내용을 입력해 주세요.")
    private String content;

    private String category;

    /** 캠퍼스 게시판 글인 경우 캠퍼스 코드 (null이면 전체 게시판) */
    private String campus;
}
