package com.ssafynity.demo.service;

import com.ssafynity.demo.domain.Bookmark;
import com.ssafynity.demo.domain.Member;
import com.ssafynity.demo.repository.BookmarkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;

    public List<Bookmark> findByMember(Member member) {
        return bookmarkRepository.findByMemberOrderByCreatedAtDesc(member);
    }

    public boolean isBookmarked(Member member, String targetType, Long targetId) {
        if (member == null) return false;
        return bookmarkRepository.existsByMemberAndTargetTypeAndTargetId(member, targetType, targetId);
    }

    @Transactional
    public boolean toggle(Member member, String targetType, Long targetId, String targetTitle) {
        if (bookmarkRepository.existsByMemberAndTargetTypeAndTargetId(member, targetType, targetId)) {
            bookmarkRepository.deleteByMemberAndTargetTypeAndTargetId(member, targetType, targetId);
            return false; // 삭제됨
        } else {
            Bookmark bookmark = Bookmark.builder()
                    .member(member)
                    .targetType(targetType)
                    .targetId(targetId)
                    .targetTitle(targetTitle)
                    .build();
            bookmarkRepository.save(bookmark);
            return true; // 추가됨
        }
    }

    @Transactional
    public void delete(Long id) {
        bookmarkRepository.deleteById(id);
    }
}
