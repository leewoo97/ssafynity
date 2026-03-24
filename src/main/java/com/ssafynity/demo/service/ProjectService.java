package com.ssafynity.demo.service;

import com.ssafynity.demo.common.exception.BusinessException;
import com.ssafynity.demo.common.exception.ErrorCode;
import com.ssafynity.demo.domain.Member;
import com.ssafynity.demo.domain.Project;
import com.ssafynity.demo.dto.request.ProjectRequest;
import com.ssafynity.demo.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projectRepository;

    @Transactional
    public Project create(ProjectRequest req, Member author) {
        Project project = Project.builder()
                .title(req.getTitle())
                .description(req.getDescription())
                .techStack(req.getTechStack())
                .githubUrl(req.getGithubUrl())
                .demoUrl(req.getDemoUrl())
                .thumbnailUrl(req.getThumbnailUrl())
                .teamSize(req.getTeamSize())
                .status(req.getStatus() != null ? req.getStatus() : "COMPLETED")
                .author(author)
                .build();
        return projectRepository.save(project);
    }

    public Project getById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
    }

    public Optional<Project> findById(Long id) {
        return projectRepository.findById(id);
    }

    @Transactional
    public Project findByIdAndIncreaseView(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
        project.setViewCount(project.getViewCount() + 1);
        return project;
    }

    @Transactional
    public void update(Long id, ProjectRequest req, Long requesterId) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
        if (!project.getAuthor().getId().equals(requesterId)) {
            throw new BusinessException(ErrorCode.PROJECT_ACCESS_DENIED);
        }
        project.setTitle(req.getTitle());
        project.setDescription(req.getDescription());
        project.setTechStack(req.getTechStack());
        project.setGithubUrl(req.getGithubUrl());
        project.setDemoUrl(req.getDemoUrl());
        project.setThumbnailUrl(req.getThumbnailUrl());
        project.setTeamSize(req.getTeamSize());
        project.setStatus(req.getStatus());
    }

    @Transactional
    public void like(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
        project.setLikeCount(project.getLikeCount() + 1);
    }

    @Transactional
    public void delete(Long id, Long requesterId, String requesterRole) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
        boolean isOwner = project.getAuthor().getId().equals(requesterId);
        boolean isAdmin = "ADMIN".equals(requesterRole);
        if (!isOwner && !isAdmin) {
            throw new BusinessException(ErrorCode.PROJECT_ACCESS_DENIED);
        }
        projectRepository.deleteById(id);
    }

    public Page<Project> findAllPaged(Pageable pageable) {
        return projectRepository.findAll(pageable);
    }

    public Page<Project> searchByKeyword(String keyword, Pageable pageable) {
        return projectRepository.findByTitleContainingIgnoreCaseOrTechStackContainingIgnoreCase(
                keyword, keyword, pageable);
    }

    public List<Project> getTopLiked() {
        return projectRepository.findTop4ByOrderByLikeCountDesc();
    }

    public List<Project> getLatest() {
        return projectRepository.findTop4ByOrderByCreatedAtDesc();
    }

    public List<Project> findByAuthor(Member author) {
        return projectRepository.findByAuthorOrderByCreatedAtDesc(author);
    }

    public List<Project> findAll() {
        return projectRepository.findAll();
    }
}
