package com.ssafynity.demo.dto.response;

import com.ssafynity.demo.domain.Report;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReportResponse {

    private Long id;
    private String targetType;
    private Long targetId;
    private String reason;
    private String reporterNickname;
    private LocalDateTime createdAt;

    public static ReportResponse from(Report report) {
        return ReportResponse.builder()
                .id(report.getId())
                .targetType(report.getTargetType())
                .targetId(report.getTargetId())
                .reason(report.getReason())
                .reporterNickname(report.getReporter() != null ? report.getReporter().getNickname() : "알 수 없음")
                .createdAt(report.getCreatedAt())
                .build();
    }
}
