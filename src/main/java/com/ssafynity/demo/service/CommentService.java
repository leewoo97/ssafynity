package com.ssafynity.demo.service;

import com.ssafynity.demo.domain.Comment;
import com.ssafynity.demo.domain.Member;
import com.ssafynity.demo.domain.Post;
import com.ssafynity.demo.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;

    @Transactional
    public Comment addComment(String content, Member author, Post post) {
        Comment comment = Comment.builder()
                .content(content)
                .author(author)
                .post(post)
                .build();
        return commentRepository.save(comment);
    }

    public List<Comment> findByPost(Post post) {
        return commentRepository.findByPost(post);
    }

    public List<Comment> findByAuthor(Member author) {
        return commentRepository.findByAuthorOrderByCreatedAtDesc(author);
    }

    @Transactional
    public void deleteComment(Long id, Member member) {
        Comment comment = commentRepository.findById(id).orElseThrow();
        if (!comment.getAuthor().getId().equals(member.getId())) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }
        commentRepository.deleteById(id);
    }

    @Transactional
    public void deleteCommentAdmin(Long id) {
        commentRepository.deleteById(id);
    }

    public long countAll() {
        return commentRepository.count();
    }
}
