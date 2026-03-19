package com.ssafynity.demo.repository;

import com.ssafynity.demo.domain.Member;
import com.ssafynity.demo.domain.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    Page<Project> findAll(Pageable pageable);
    Page<Project> findByTitleContainingIgnoreCaseOrTechStackContainingIgnoreCase(
            String title, String techStack, Pageable pageable);
    List<Project> findTop4ByOrderByLikeCountDesc();
    List<Project> findTop4ByOrderByCreatedAtDesc();
    List<Project> findByAuthorOrderByCreatedAtDesc(Member author);
}
