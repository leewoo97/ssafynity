package com.ssafynity.demo.dto.response;

import com.ssafynity.demo.domain.Post;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PostResponse {

    private Long id;
    private String title;
    private String content;
    private String category;
    private String campus;
    private int viewCount;
    private int likeCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 작성자 정보 (별도 조회 없이 포함)
    private Long authorId;
    private String authorNickname;
    private String authorProfileImageUrl;
    private String authorBio;

    public static PostResponse from(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .category(post.getCategory())
                .campus(post.getCampus())
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .authorId(post.getAuthor() != null ? post.getAuthor().getId() : null)
                .authorNickname(post.getAuthor() != null ? post.getAuthor().getNickname() : null)
                .authorProfileImageUrl(post.getAuthor() != null ? post.getAuthor().getProfileImageUrl() : null)
                .authorBio(post.getAuthor() != null ? post.getAuthor().getBio() : null)
                .build();
    }
}
