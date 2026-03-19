package com.ssafynity.demo.repository;

import com.ssafynity.demo.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface PostRepositoryCustom {
    /**
     * QueryDSL 기반 고급 검색: 제목+본문 모두 검색, 카테고리/정렬 필터링
     */
    Page<Post> searchAdvanced(String keyword, String category, String sort, Pageable pageable);

    /**
     * 최근 N일 이내 조회수+추천수 합산 기준 인기글
     */
    List<Post> findHotPosts(LocalDateTime since, int limit);
}
