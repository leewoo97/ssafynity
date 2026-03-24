package com.ssafynity.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReportRequest {

    @NotBlank(message = "신고 대상 유형을 선택해 주세요.")
    private String targetType;   // POST, COMMENT

    private Long targetId;

    @NotBlank(message = "신고 사유를 입력해 주세요.")
    private String reason;
}
