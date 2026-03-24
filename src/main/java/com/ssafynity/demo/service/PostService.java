package com.ssafynity.demo.service;

import com.ssafynity.demo.common.exception.BusinessException;
import com.ssafynity.demo.common.exception.ErrorCode;
import com.ssafynity.demo.domain.Member;
import com.ssafynity.demo.domain.Post;
import com.ssafynity.demo.dto.request.PostRequest;
import com.ssafynity.demo.repository.PostRepository;
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
public class PostService {

    private final PostRepository postRepository;

    @Transactional
    public Post createPost(PostRequest req, Member author) {
        Post post = Post.builder()
                .title(req.getTitle())
                .content(req.getContent())
                .category(req.getCategory() != null && !req.getCategory().isEmpty() ? req.getCategory() : "일반")
                .campus(req.getCampus())
                .author(author)
                .build();
        return postRepository.save(post);
    }

    public Post getById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
    }

    public Optional<Post> findById(Long id) {
        return postRepository.findById(id);
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

    @Transactional
    public Post findByIdAndIncreaseView(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
        post.setViewCount(post.getViewCount() + 1);
        return post;
    }

    @Transactional
    public void updatePost(Long id, PostRequest req, Long requesterId, String requesterRole) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
        boolean isOwner = post.getAuthor().getId().equals(requesterId);
        boolean isAdmin = "ADMIN".equals(requesterRole);
        if (!isOwner && !isAdmin) {
            throw new BusinessException(ErrorCode.POST_ACCESS_DENIED);
        }
        post.setTitle(req.getTitle());
        post.setContent(req.getContent());
        if (req.getCategory() != null && !req.getCategory().isEmpty()) {
            post.setCategory(req.getCategory());
        }
    }

    @Transactional
    public void deletePost(Long id, Long requesterId, String requesterRole) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
        boolean isOwner = post.getAuthor().getId().equals(requesterId);
        boolean isAdmin = "ADMIN".equals(requesterRole);
        if (!isOwner && !isAdmin) {
            throw new BusinessException(ErrorCode.POST_ACCESS_DENIED);
        }
        postRepository.deleteById(id);
    }

    public Page<Post> searchAdvanced(String keyword, String category, String sort, Pageable pageable) {
        return postRepository.searchAdvanced(keyword, category, sort, pageable);
    }

    public List<Post> getHotPosts(int days, int limit) {
        java.time.LocalDateTime since = java.time.LocalDateTime.now().minusDays(days);
        return postRepository.findHotPosts(since, limit);
    }

    @Transactional
    public Post createCampusPost(PostRequest req, Member author) {
        Post post = Post.builder()
                .title(req.getTitle())
                .content(req.getContent())
                .category("캠퍼스")
                .author(author)
                .campus(req.getCampus())
                .build();
        return postRepository.save(post);
    }

    public List<Post> getRecentCampusPosts(String campus) {
        return postRepository.findTop5ByCampusOrderByCreatedAtDesc(campus);
    }

    public Page<Post> findByCampusPaged(String campus, Pageable pageable) {
        return postRepository.findByCampus(campus, pageable);
    }

    public Page<Post> findByCampusAndKeyword(String campus, String keyword, Pageable pageable) {
        return postRepository.findByCampusAndTitleContainingIgnoreCase(campus, keyword, pageable);
    }
}
