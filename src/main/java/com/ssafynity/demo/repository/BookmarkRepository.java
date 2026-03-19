package com.ssafynity.demo.repository;

import com.ssafynity.demo.domain.Bookmark;
import com.ssafynity.demo.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    List<Bookmark> findByMemberOrderByCreatedAtDesc(Member member);
    Optional<Bookmark> findByMemberAndTargetTypeAndTargetId(Member member, String targetType, Long targetId);
    boolean existsByMemberAndTargetTypeAndTargetId(Member member, String targetType, Long targetId);
    void deleteByMemberAndTargetTypeAndTargetId(Member member, String targetType, Long targetId);
}
