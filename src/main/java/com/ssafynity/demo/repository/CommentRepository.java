package com.ssafynity.demo.repository;

import com.ssafynity.demo.domain.Comment;
import com.ssafynity.demo.domain.Member;
import com.ssafynity.demo.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPost(Post post);
    List<Comment> findByAuthorOrderByCreatedAtDesc(Member author);
}
