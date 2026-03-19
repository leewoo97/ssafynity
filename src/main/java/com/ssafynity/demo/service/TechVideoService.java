package com.ssafynity.demo.service;

import com.ssafynity.demo.domain.Member;
import com.ssafynity.demo.domain.TechVideo;
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
public class TechVideoService {

    private final TechVideoRepository techVideoRepository;

    private static final String[] CATEGORIES = {"강의", "세미나", "코드리뷰", "프로젝트발표", "기타"};

    public String[] getCategories() {
        return CATEGORIES;
    }

    /**
     * YouTube URL → 영상 ID 추출
     */
    public String extractYoutubeId(String url) {
        if (url == null || url.isBlank()) return "";
        if (url.contains("watch?v=")) return url.split("watch\\?v=")[1].split("&")[0];
        if (url.contains("youtu.be/"))  return url.split("youtu.be/")[1].split("\\?")[0];
        if (url.contains("/embed/"))    return url.split("/embed/")[1].split("\\?")[0];
        return url; // 이미 ID만 입력한 경우
    }

    @Transactional
    public TechVideo create(String title, String description, String youtubeUrl,
                            String duration, String category, String tags, Member author) {
        TechVideo video = TechVideo.builder()
                .title(title)
                .description(description)
                .youtubeId(extractYoutubeId(youtubeUrl))
                .duration(duration)
                .category(category != null && !category.isBlank() ? category : "기타")
                .tags(tags)
                .author(author)
                .build();
        return techVideoRepository.save(video);
    }

    public Optional<TechVideo> findById(Long id) {
        return techVideoRepository.findById(id);
    }

    @Transactional
    public TechVideo findByIdAndIncreaseView(Long id) {
        TechVideo video = techVideoRepository.findById(id).orElseThrow();
        video.setViewCount(video.getViewCount() + 1);
        return video;
    }

    @Transactional
    public void update(Long id, String title, String description,
                       String youtubeUrl, String duration, String category, String tags) {
        TechVideo video = techVideoRepository.findById(id).orElseThrow();
        video.setTitle(title);
        video.setDescription(description);
        video.setYoutubeId(extractYoutubeId(youtubeUrl));
        video.setDuration(duration);
        video.setCategory(category);
        video.setTags(tags);
    }

    @Transactional
    public void delete(Long id) {
        techVideoRepository.deleteById(id);
    }

    @Transactional
    public void togglePin(Long id) {
        TechVideo v = techVideoRepository.findById(id).orElseThrow();
        v.setPinned(!v.isPinned());
    }

    public Page<TechVideo> findAllPaged(Pageable pageable) {
        return techVideoRepository.findAll(pageable);
    }

    public Page<TechVideo> findByCategory(String category, Pageable pageable) {
        return techVideoRepository.findByCategory(category, pageable);
    }

    public Page<TechVideo> searchByTitle(String keyword, Pageable pageable) {
        return techVideoRepository.findByTitleContainingIgnoreCase(keyword, pageable);
    }

    public List<TechVideo> getTopViewed() {
        return techVideoRepository.findTop5ByOrderByViewCountDesc();
    }

    public List<TechVideo> getLatest() {
        return techVideoRepository.findTop6ByOrderByCreatedAtDesc();
    }

    public List<TechVideo> getPinned() {
        return techVideoRepository.findTop4ByPinnedTrueOrderByCreatedAtDesc();
    }

    public List<TechVideo> findByAuthor(Member author) {
        return techVideoRepository.findByAuthorOrderByCreatedAtDesc(author);
    }

    public List<TechVideo> findAll() {
        return techVideoRepository.findAll();
    }
}
