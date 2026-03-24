package com.ssafynity.demo.service;

import com.ssafynity.demo.common.exception.BusinessException;
import com.ssafynity.demo.common.exception.ErrorCode;
import com.ssafynity.demo.domain.Member;
import com.ssafynity.demo.domain.TechDoc;
import com.ssafynity.demo.dto.request.TechDocRequest;
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
@Transactional(readOnly = true)
public class TechDocService {

    private final TechDocRepository techDocRepository;

    private static final String[] CATEGORIES = {
        "튜토리얼", "아키텍처", "알고리즘", "DevOps", "데이터베이스", "프론트엔드", "백엔드", "기타"
    };

    public String[] getCategories() { return CATEGORIES; }

    @Transactional
    public TechDoc create(TechDocRequest req, Member author) {
        TechDoc doc = TechDoc.builder()
                .title(req.getTitle())
                .content(req.getContent())
                .category(req.getCategory() != null && !req.getCategory().isBlank() ? req.getCategory() : "기타")
                .tags(req.getTags())
                .markdown(req.isMarkdown())
                .author(author)
                .build();
        return techDocRepository.save(doc);
    }

    public TechDoc getById(Long id) {
        return techDocRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.TECH_DOC_NOT_FOUND));
    }

    public Optional<TechDoc> findById(Long id) {
        return techDocRepository.findById(id);
    }

    @Transactional
    public TechDoc findByIdAndIncreaseView(Long id) {
        TechDoc doc = techDocRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.TECH_DOC_NOT_FOUND));
        doc.setViewCount(doc.getViewCount() + 1);
        return doc;
    }

    @Transactional
    public void update(Long id, TechDocRequest req, Long requesterId) {
        TechDoc doc = techDocRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.TECH_DOC_NOT_FOUND));
        if (!doc.getAuthor().getId().equals(requesterId)) {
            throw new BusinessException(ErrorCode.TECH_DOC_ACCESS_DENIED);
        }
        doc.setTitle(req.getTitle());
        doc.setContent(req.getContent());
        doc.setCategory(req.getCategory());
        doc.setTags(req.getTags());
    }

    @Transactional
    public void delete(Long id, Long requesterId, String requesterRole) {
        TechDoc doc = techDocRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.TECH_DOC_NOT_FOUND));
        boolean isOwner = doc.getAuthor().getId().equals(requesterId);
        boolean isAdmin = "ADMIN".equals(requesterRole);
        if (!isOwner && !isAdmin) {
            throw new BusinessException(ErrorCode.TECH_DOC_ACCESS_DENIED);
        }
        techDocRepository.deleteById(id);
    }

    @Transactional
    public void togglePin(Long id) {
        TechDoc doc = techDocRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.TECH_DOC_NOT_FOUND));
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
