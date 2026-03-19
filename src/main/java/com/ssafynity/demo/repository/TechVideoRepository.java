package com.ssafynity.demo.repository;

import com.ssafynity.demo.domain.Member;
import com.ssafynity.demo.domain.TechVideo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TechVideoRepository extends JpaRepository<TechVideo, Long> {
    Page<TechVideo> findAll(Pageable pageable);
    Page<TechVideo> findByCategory(String category, Pageable pageable);
    Page<TechVideo> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);
    List<TechVideo> findTop5ByOrderByViewCountDesc();
    List<TechVideo> findTop4ByPinnedTrueOrderByCreatedAtDesc();
    List<TechVideo> findTop6ByOrderByCreatedAtDesc();
    List<TechVideo> findByAuthorOrderByCreatedAtDesc(Member author);
}
