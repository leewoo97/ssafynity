package com.ssafynity.demo.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class EventRequest {

    @NotBlank(message = "제목을 입력해 주세요.")
    private String title;

    private String description;

    @NotNull(message = "시작 날짜를 입력해 주세요.")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startDate;

    @NotNull(message = "종료 날짜를 입력해 주세요.")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime endDate;

    private String location = "ONLINE";
    private String eventType = "기타";

    @Min(value = 0, message = "최대 참가자 수는 0 이상이어야 합니다.")
    private int maxParticipants = 0;
}
