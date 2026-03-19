package com.ssafynity.demo.repository;

import com.ssafynity.demo.domain.TechDoc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TechDocRepositoryCustom {
    Page<TechDoc> searchDocs(String keyword, String category, Pageable pageable);
}
