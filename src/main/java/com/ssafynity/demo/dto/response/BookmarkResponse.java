package com.ssafynity.demo.dto.response;

import com.ssafynity.demo.domain.Bookmark;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BookmarkResponse {

    private Long id;
    private String targetType;
    private Long targetId;
    private String targetTitle;
    private LocalDateTime createdAt;

    public static BookmarkResponse from(Bookmark bookmark) {
        return BookmarkResponse.builder()
                .id(bookmark.getId())
                .targetType(bookmark.getTargetType())
                .targetId(bookmark.getTargetId())
                .targetTitle(bookmark.getTargetTitle())
                .createdAt(bookmark.getCreatedAt())
                .build();
    }
}
