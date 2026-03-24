package com.ssafynity.demo.dto.response;

import com.ssafynity.demo.domain.TechDoc;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class TechDocResponse {

    private Long id;
    private String title;
    private String content;
    private String category;
    private String tags;
    private boolean markdown;
    private boolean pinned;
    private int viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Long authorId;
    private String authorNickname;

    public static TechDocResponse from(TechDoc doc) {
        return TechDocResponse.builder()
                .id(doc.getId())
                .title(doc.getTitle())
                .content(doc.getContent())
                .category(doc.getCategory())
                .tags(doc.getTags())
                .markdown(doc.isMarkdown())
                .pinned(doc.isPinned())
                .viewCount(doc.getViewCount())
                .createdAt(doc.getCreatedAt())
                .updatedAt(doc.getUpdatedAt())
                .authorId(doc.getAuthor() != null ? doc.getAuthor().getId() : null)
                .authorNickname(doc.getAuthor() != null ? doc.getAuthor().getNickname() : null)
                .build();
    }
}
