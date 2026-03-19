package com.ssafynity.demo.service;

import com.ssafynity.demo.domain.Member;
import com.ssafynity.demo.domain.Post;
import com.ssafynity.demo.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;

    @Transactional
    public Post createPost(String title, String content, String category, Member author) {
        Post post = Post.builder()
                .title(title)
                .content(content)
                .category(category != null && !category.isEmpty() ? category : "일반")
                .author(author)
                .build();
        return postRepository.save(post);
    }

    public List<Post> findAll() {
        return postRepository.findAll();
    }

    public List<Post> searchByTitle(String keyword) {
        return postRepository.findByTitleContainingIgnoreCase(keyword);
    }

    public Page<Post> findAllPaged(Pageable pageable) {
        return postRepository.findAll(pageable);
    }

    public Page<Post> searchByTitlePaged(String keyword, Pageable pageable) {
        return postRepository.findByTitleContainingIgnoreCase(keyword, pageable);
    }

    public Page<Post> findByCategory(String category, Pageable pageable) {
        return postRepository.findByCategory(category, pageable);
    }

    public Page<Post> findByCategoryAndKeyword(String category, String keyword, Pageable pageable) {
        return postRepository.findByCategoryAndTitleContainingIgnoreCase(category, keyword, pageable);
    }

    public List<Post> getTopViewedPosts() {
        return postRepository.findTop5ByOrderByViewCountDesc();
    }

    public List<Post> getTopLikedPosts() {
        return postRepository.findTop5ByOrderByLikeCountDesc();
    }

    public List<Post> findByAuthor(Member author) {
        return postRepository.findByAuthorOrderByCreatedAtDesc(author);
    }

    public Optional<Post> findById(Long id) {
        return postRepository.findById(id);
    }

    @Transactional
    public Post findByIdAndIncreaseView(Long id) {
        Post post = postRepository.findById(id).orElseThrow();
        post.setViewCount(post.getViewCount() + 1);
        return post;
    }

    @Transactional
    public void updatePost(Long id, String title, String content, String category) {
        Post post = postRepository.findById(id).orElseThrow();
        post.setTitle(title);
        post.setContent(content);
        if (category != null && !category.isEmpty()) post.setCategory(category);
    }

    @Transactional
    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }

    // ── QueryDSL 고급 검색 ──────────────────────────────────────────
    public Page<Post> searchAdvanced(String keyword, String category, String sort, Pageable pageable) {
        return postRepository.searchAdvanced(keyword, category, sort, pageable);
    }

    public List<Post> getHotPosts(int days, int limit) {
        java.time.LocalDateTime since = java.time.LocalDateTime.now().minusDays(days);
        return postRepository.findHotPosts(since, limit);
    }
}
