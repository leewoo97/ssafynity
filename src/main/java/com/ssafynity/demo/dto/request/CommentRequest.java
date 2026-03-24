package com.ssafynity.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CommentRequest {

    @NotBlank(message = "내용을 입력해 주세요.")
    private String content;
}
