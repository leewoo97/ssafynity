package com.ssafynity.demo.service;

import com.ssafynity.demo.domain.Member;
import com.ssafynity.demo.domain.TechDoc;
import com.ssafynity.demo.repository.TechDocRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TechDocService {

    private final TechDocRepository techDocRepository;

    private static final String[] CATEGORIES = {
        "튜토리얼", "아키텍처", "알고리즘", "DevOps", "데이터베이스", "프론트엔드", "백엔드", "기타"
    };

    public String[] getCategories() {
        return CATEGORIES;
    }

    @Transactional
    public TechDoc create(String title, String content, String category, String tags, Member author) {
        TechDoc doc = TechDoc.builder()
                .title(title)
                .content(content)
                .category(category != null && !category.isBlank() ? category : "기타")
                .tags(tags)
                .author(author)
                .build();
        return techDocRepository.save(doc);
    }

    public Optional<TechDoc> findById(Long id) {
        return techDocRepository.findById(id);
    }

    @Transactional
    public TechDoc findByIdAndIncreaseView(Long id) {
        TechDoc doc = techDocRepository.findById(id).orElseThrow();
        doc.setViewCount(doc.getViewCount() + 1);
        return doc;
    }

    @Transactional
    public void update(Long id, String title, String content, String category, String tags) {
        TechDoc doc = techDocRepository.findById(id).orElseThrow();
        doc.setTitle(title);
        doc.setContent(content);
        doc.setCategory(category);
        doc.setTags(tags);
    }

    @Transactional
    public void delete(Long id) {
        techDocRepository.deleteById(id);
    }

    @Transactional
    public void togglePin(Long id) {
        TechDoc doc = techDocRepository.findById(id).orElseThrow();
        doc.setPinned(!doc.isPinned());
    }

    public Page<TechDoc> searchDocs(String keyword, String category, Pageable pageable) {
        return techDocRepository.searchDocs(keyword, category, pageable);
    }

    public List<TechDoc> getTopViewed() {
        return techDocRepository.findTop5ByOrderByViewCountDesc();
    }

    public List<TechDoc> getPinned() {
        return techDocRepository.findTop5ByPinnedTrueOrderByCreatedAtDesc();
    }

    public List<TechDoc> getLatest() {
        return techDocRepository.findTop6ByOrderByCreatedAtDesc();
    }

    public List<TechDoc> findByAuthor(Member author) {
        return techDocRepository.findByAuthorOrderByCreatedAtDesc(author);
    }

    public List<TechDoc> findAll() {
        return techDocRepository.findAll();
    }
}
