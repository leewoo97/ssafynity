package com.ssafynity.demo.repository;

import com.ssafynity.demo.domain.PostLike;
import com.ssafynity.demo.domain.Member;
import com.ssafynity.demo.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    Optional<PostLike> findByMemberAndPost(Member member, Post post);
    boolean existsByMemberAndPost(Member member, Post post);
    int countByPost(Post post);
}
