package com.ssafynity.demo.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "아이디를 입력해 주세요.")
    @Pattern(regexp = "^[a-z0-9_]{4,20}$", message = "아이디는 영소문자·숫자·_로 4~20자여야 합니다.")
    private String username;

    @NotBlank(message = "비밀번호를 입력해 주세요.")
    @Size(min = 6, message = "비밀번호는 6자 이상이어야 합니다.")
    private String password;

    @NotBlank(message = "닉네임을 입력해 주세요.")
    @Size(max = 20, message = "닉네임은 20자 이하여야 합니다.")
    private String nickname;

    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    private String realName;

    @NotBlank(message = "캠퍼스를 선택해 주세요.")
    private String campus;

    @NotNull(message = "기수를 입력해 주세요.")
    @Min(value = 1, message = "기수는 1 이상이어야 합니다.")
    private Integer cohort;

    @NotNull(message = "반을 선택해 주세요.")
    private Integer classCode;
}
