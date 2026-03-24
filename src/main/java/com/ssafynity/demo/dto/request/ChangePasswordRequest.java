package com.ssafynity.demo.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChangePasswordRequest {

    @NotBlank(message = "현재 비밀번호를 입력해 주세요.")
    private String currentPassword;

    @NotBlank(message = "새 비밀번호를 입력해 주세요.")
    @Size(min = 6, message = "비밀번호는 6자 이상이어야 합니다.")
    private String newPassword;
}
