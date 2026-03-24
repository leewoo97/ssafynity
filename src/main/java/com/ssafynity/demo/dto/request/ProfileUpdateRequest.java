package com.ssafynity.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProfileUpdateRequest {

    @NotBlank(message = "닉네임을 입력해 주세요.")
    @Size(max = 20, message = "닉네임은 20자 이하여야 합니다.")
    private String nickname;

    private String email;
    private String bio;
    private String profileImageUrl;
    private String realName;
    private String campus;
    private Integer cohort;
    private Integer classCode;
}
