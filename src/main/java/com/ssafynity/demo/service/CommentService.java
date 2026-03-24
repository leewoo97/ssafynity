package com.ssafynity.demo.service;

import com.ssafynity.demo.common.exception.BusinessException;
import com.ssafynity.demo.common.exception.ErrorCode;
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
@Transactional(readOnly = true)
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
    public void deleteComment(Long id, Long requesterId, String requesterRole) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));
        boolean isOwner = comment.getAuthor().getId().equals(requesterId);
        boolean isAdmin = "ADMIN".equals(requesterRole);
        if (!isOwner && !isAdmin) {
            throw new BusinessException(ErrorCode.COMMENT_ACCESS_DENIED);
        }
        commentRepository.deleteById(id);
    }

    public long countAll() {
        return commentRepository.count();
    }
}
