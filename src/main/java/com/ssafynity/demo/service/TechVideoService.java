package com.ssafynity.demo.service;

import com.ssafynity.demo.common.exception.BusinessException;
import com.ssafynity.demo.common.exception.ErrorCode;
import com.ssafynity.demo.domain.Member;
import com.ssafynity.demo.domain.TechVideo;
import com.ssafynity.demo.dto.request.TechVideoRequest;
import com.ssafynity.demo.repository.TechVideoRepository;
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
public class TechVideoService {

    private final TechVideoRepository techVideoRepository;

    private static final String[] CATEGORIES = {"강의", "세미나", "코드리뷰", "프로젝트발표", "기타"};

    public String[] getCategories() { return CATEGORIES; }

    public String extractYoutubeId(String url) {
        if (url == null || url.isBlank()) return "";
        if (url.contains("watch?v=")) return url.split("watch\\?v=")[1].split("&")[0];
        if (url.contains("youtu.be/")) return url.split("youtu.be/")[1].split("\\?")[0];
        if (url.contains("/embed/"))   return url.split("/embed/")[1].split("\\?")[0];
        return url;
    }

    @Transactional
    public TechVideo create(TechVideoRequest req, Member author) {
        TechVideo video = TechVideo.builder()
                .title(req.getTitle())
                .description(req.getDescription())
                .youtubeId(extractYoutubeId(req.getYoutubeUrl()))
                .duration(req.getDuration())
                .category(req.getCategory() != null && !req.getCategory().isBlank() ? req.getCategory() : "기타")
                .tags(req.getTags())
                .author(author)
                .build();
        return techVideoRepository.save(video);
    }

    public TechVideo getById(Long id) {
        return techVideoRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.TECH_VIDEO_NOT_FOUND));
    }

    public Optional<TechVideo> findById(Long id) {
        return techVideoRepository.findById(id);
    }

    @Transactional
    public TechVideo findByIdAndIncreaseView(Long id) {
        TechVideo video = techVideoRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.TECH_VIDEO_NOT_FOUND));
        video.setViewCount(video.getViewCount() + 1);
        return video;
    }

    @Transactional
    public void update(Long id, TechVideoRequest req, Long requesterId) {
        TechVideo video = techVideoRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.TECH_VIDEO_NOT_FOUND));
        if (!video.getAuthor().getId().equals(requesterId)) {
            throw new BusinessException(ErrorCode.TECH_VIDEO_ACCESS_DENIED);
        }
        video.setTitle(req.getTitle());
        video.setDescription(req.getDescription());
        video.setYoutubeId(extractYoutubeId(req.getYoutubeUrl()));
        video.setDuration(req.getDuration());
        video.setCategory(req.getCategory());
        video.setTags(req.getTags());
    }

    @Transactional
    public void delete(Long id, Long requesterId, String requesterRole) {
        TechVideo video = techVideoRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.TECH_VIDEO_NOT_FOUND));
        boolean isOwner = video.getAuthor().getId().equals(requesterId);
        boolean isAdmin = "ADMIN".equals(requesterRole);
        if (!isOwner && !isAdmin) {
            throw new BusinessException(ErrorCode.TECH_VIDEO_ACCESS_DENIED);
        }
        techVideoRepository.deleteById(id);
    }

    @Transactional
    public void togglePin(Long id) {
        TechVideo v = techVideoRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.TECH_VIDEO_NOT_FOUND));
        v.setPinned(!v.isPinned());
    }

    public Page<TechVideo> findAllPaged(Pageable pageable) { return techVideoRepository.findAll(pageable); }
    public Page<TechVideo> findByCategory(String category, Pageable pageable) { return techVideoRepository.findByCategory(category, pageable); }
    public Page<TechVideo> searchByTitle(String keyword, Pageable pageable) { return techVideoRepository.findByTitleContainingIgnoreCase(keyword, pageable); }
    public List<TechVideo> getTopViewed() { return techVideoRepository.findTop5ByOrderByViewCountDesc(); }
    public List<TechVideo> getLatest() { return techVideoRepository.findTop6ByOrderByCreatedAtDesc(); }
    public List<TechVideo> getPinned() { return techVideoRepository.findTop4ByPinnedTrueOrderByCreatedAtDesc(); }
    public List<TechVideo> findByAuthor(Member author) { return techVideoRepository.findByAuthorOrderByCreatedAtDesc(author); }
    public List<TechVideo> findAll() { return techVideoRepository.findAll(); }
}
