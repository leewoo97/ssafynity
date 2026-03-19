package com.ssafynity.demo.service;

import com.ssafynity.demo.domain.Member;
import com.ssafynity.demo.domain.Post;
import com.ssafynity.demo.domain.PostLike;
import com.ssafynity.demo.repository.PostLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostLikeService {
    private final PostLikeRepository postLikeRepository;

    public boolean isLiked(Member member, Post post) {
        if (member == null) return false;
        return postLikeRepository.existsByMemberAndPost(member, post);
    }

    public int getLikeCount(Post post) {
        return postLikeRepository.countByPost(post);
    }

    @Transactional
    public boolean toggleLike(Member member, Post post) {
        return postLikeRepository.findByMemberAndPost(member, post)
                .map(like -> {
                    postLikeRepository.delete(like);
                    post.setLikeCount(post.getLikeCount() - 1);
                    return false;
                })
                .orElseGet(() -> {
                    PostLike like = PostLike.builder().member(member).post(post).build();
                    postLikeRepository.save(like);
                    post.setLikeCount(post.getLikeCount() + 1);
                    return true;
                });
    }
}
