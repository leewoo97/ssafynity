package com.ssafynity.demo.dto.response;

import com.ssafynity.demo.domain.Comment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CommentResponse {

    private Long id;
    private String content;
    private LocalDateTime createdAt;

    private Long authorId;
    private String authorNickname;
    private String authorProfileImageUrl;

    public static CommentResponse from(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .authorId(comment.getAuthor() != null ? comment.getAuthor().getId() : null)
                .authorNickname(comment.getAuthor() != null ? comment.getAuthor().getNickname() : null)
                .authorProfileImageUrl(comment.getAuthor() != null ? comment.getAuthor().getProfileImageUrl() : null)
                .build();
    }
}
