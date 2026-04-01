package com.ssafynity.demo.chat.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class GroupChatRequest {

    @NotBlank(message = "그룹 이름을 입력해 주세요.")
    private String name;

    @NotEmpty(message = "최소 1명 이상 초대해야 합니다.")
    private List<Long> memberIds;
}
