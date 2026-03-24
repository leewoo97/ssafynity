package com.ssafynity.demo.dto.response;

import com.ssafynity.demo.domain.Project;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ProjectResponse {

    private Long id;
    private String title;
    private String description;
    private String techStack;
    private String githubUrl;
    private String demoUrl;
    private String thumbnailUrl;
    private int teamSize;
    private String status;
    private int likeCount;
    private int viewCount;
    private LocalDateTime createdAt;

    private Long authorId;
    private String authorNickname;
    private String authorProfileImageUrl;

    public static ProjectResponse from(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .title(project.getTitle())
                .description(project.getDescription())
                .techStack(project.getTechStack())
                .githubUrl(project.getGithubUrl())
                .demoUrl(project.getDemoUrl())
                .thumbnailUrl(project.getThumbnailUrl())
                .teamSize(project.getTeamSize())
                .status(project.getStatus())
                .likeCount(project.getLikeCount())
                .viewCount(project.getViewCount())
                .createdAt(project.getCreatedAt())
                .authorId(project.getAuthor() != null ? project.getAuthor().getId() : null)
                .authorNickname(project.getAuthor() != null ? project.getAuthor().getNickname() : null)
                .authorProfileImageUrl(project.getAuthor() != null ? project.getAuthor().getProfileImageUrl() : null)
                .build();
    }
}
