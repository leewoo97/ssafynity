package com.ssafynity.demo.repository;

import com.ssafynity.demo.domain.Member;
import com.ssafynity.demo.domain.TechDoc;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TechDocRepository extends JpaRepository<TechDoc, Long>, TechDocRepositoryCustom {
    List<TechDoc> findTop5ByOrderByViewCountDesc();
    List<TechDoc> findTop5ByPinnedTrueOrderByCreatedAtDesc();
    List<TechDoc> findByAuthorOrderByCreatedAtDesc(Member author);
    List<TechDoc> findTop6ByOrderByCreatedAtDesc();
}
