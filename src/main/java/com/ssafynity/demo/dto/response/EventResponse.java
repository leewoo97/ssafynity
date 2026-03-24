package com.ssafynity.demo.dto.response;

import com.ssafynity.demo.domain.Event;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class EventResponse {

    private Long id;
    private String title;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String location;
    private String eventType;
    private String status;
    private int maxParticipants;
    private int currentParticipants;
    private LocalDateTime createdAt;

    private Long organizerId;
    private String organizerNickname;

    public static EventResponse from(Event event) {
        return EventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .location(event.getLocation())
                .eventType(event.getEventType())
                .status(event.getStatus())
                .maxParticipants(event.getMaxParticipants())
                .currentParticipants(event.getCurrentParticipants())
                .createdAt(event.getCreatedAt())
                .organizerId(event.getOrganizer() != null ? event.getOrganizer().getId() : null)
                .organizerNickname(event.getOrganizer() != null ? event.getOrganizer().getNickname() : null)
                .build();
    }
}
