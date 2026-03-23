package com.ssafynity.demo.repository;

import com.ssafynity.demo.domain.Member;
import com.ssafynity.demo.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {
    List<Post> findByTitleContainingIgnoreCase(String keyword);
    Page<Post> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);
    Page<Post> findAll(Pageable pageable);
    Page<Post> findByCategory(String category, Pageable pageable);
    Page<Post> findByCategoryAndTitleContainingIgnoreCase(String category, String keyword, Pageable pageable);
    List<Post> findTop5ByOrderByViewCountDesc();
    List<Post> findTop5ByOrderByLikeCountDesc();
    List<Post> findByAuthorOrderByCreatedAtDesc(Member author);

    // ── 캠퍼스 게시판 ──────────────────────────────────────────────
    List<Post> findTop5ByCampusOrderByCreatedAtDesc(String campus);
    Page<Post> findByCampus(String campus, Pageable pageable);
    Page<Post> findByCampusAndTitleContainingIgnoreCase(String campus, String keyword, Pageable pageable);
}
