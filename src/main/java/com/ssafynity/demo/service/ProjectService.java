package com.ssafynity.demo.service;

import com.ssafynity.demo.domain.Member;
import com.ssafynity.demo.domain.Project;
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
public class ProjectService {

    private final ProjectRepository projectRepository;

    @Transactional
    public Project create(String title, String description, String techStack,
                          String githubUrl, String demoUrl, String thumbnailUrl,
                          int teamSize, String status, Member author) {
        Project project = Project.builder()
                .title(title)
                .description(description)
                .techStack(techStack)
                .githubUrl(githubUrl)
                .demoUrl(demoUrl)
                .thumbnailUrl(thumbnailUrl)
                .teamSize(teamSize)
                .status(status != null ? status : "COMPLETED")
                .author(author)
                .build();
        return projectRepository.save(project);
    }

    public Optional<Project> findById(Long id) {
        return projectRepository.findById(id);
    }

    @Transactional
    public Project findByIdAndIncreaseView(Long id) {
        Project project = projectRepository.findById(id).orElseThrow();
        project.setViewCount(project.getViewCount() + 1);
        return project;
    }

    @Transactional
    public void update(Long id, String title, String description, String techStack,
                       String githubUrl, String demoUrl, String thumbnailUrl,
                       int teamSize, String status) {
        Project project = projectRepository.findById(id).orElseThrow();
        project.setTitle(title);
        project.setDescription(description);
        project.setTechStack(techStack);
        project.setGithubUrl(githubUrl);
        project.setDemoUrl(demoUrl);
        project.setThumbnailUrl(thumbnailUrl);
        project.setTeamSize(teamSize);
        project.setStatus(status);
    }

    @Transactional
    public void like(Long id) {
        Project project = projectRepository.findById(id).orElseThrow();
        project.setLikeCount(project.getLikeCount() + 1);
    }

    @Transactional
    public void delete(Long id) {
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
