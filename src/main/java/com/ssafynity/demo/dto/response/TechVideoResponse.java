package com.ssafynity.demo.dto.response;

import com.ssafynity.demo.domain.TechVideo;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class TechVideoResponse {

    private Long id;
    private String title;
    private String description;
    private String youtubeId;
    private String duration;
    private String category;
    private String tags;
    private boolean pinned;
    private int viewCount;
    private LocalDateTime createdAt;

    private Long authorId;
    private String authorNickname;

    public static TechVideoResponse from(TechVideo video) {
        return TechVideoResponse.builder()
                .id(video.getId())
                .title(video.getTitle())
                .description(video.getDescription())
                .youtubeId(video.getYoutubeId())
                .duration(video.getDuration())
                .category(video.getCategory())
                .tags(video.getTags())
                .pinned(video.isPinned())
                .viewCount(video.getViewCount())
                .createdAt(video.getCreatedAt())
                .authorId(video.getAuthor() != null ? video.getAuthor().getId() : null)
                .authorNickname(video.getAuthor() != null ? video.getAuthor().getNickname() : null)
                .build();
    }
}
