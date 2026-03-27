package com.ssafynity.demo.repository;

import com.ssafynity.demo.domain.DirectRoom;
import com.ssafynity.demo.domain.DirectRoomMember;
import com.ssafynity.demo.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface DirectRoomMemberRepository extends JpaRepository<DirectRoomMember, Long> {

    List<DirectRoomMember> findByRoom(DirectRoom room);

    boolean existsByRoomAndMember(DirectRoom room, Member member);

    java.util.Optional<DirectRoomMember> findByRoomAndMember(DirectRoom room, Member member);

    /** message.createdAt 이후에 lastReadAt 이 있는 멤버 수 (= 읽은 사람 수) */
    @Query("SELECT COUNT(drm) FROM DirectRoomMember drm " +
           "WHERE drm.room = :room " +
           "AND drm.member.id != :senderId " +
           "AND drm.lastReadAt IS NOT NULL " +
           "AND drm.lastReadAt >= :msgCreatedAt")
    long countReadersAfter(@Param("room") DirectRoom room,
                           @Param("senderId") Long senderId,
                           @Param("msgCreatedAt") java.time.LocalDateTime msgCreatedAt);

    @Modifying
    @Transactional
    @Query("DELETE FROM DirectRoomMember drm WHERE drm.room = :room AND drm.member = :member")
    void deleteByRoomAndMember(@Param("room") DirectRoom room, @Param("member") Member member);
}
