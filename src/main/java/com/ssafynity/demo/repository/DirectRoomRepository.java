package com.ssafynity.demo.repository;

import com.ssafynity.demo.domain.DirectRoom;
import com.ssafynity.demo.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DirectRoomRepository extends JpaRepository<DirectRoom, Long> {

    /** 특정 멤버가 참여 중인 모든 방 */
    @Query("SELECT drm.room FROM DirectRoomMember drm WHERE drm.member = :member")
    List<DirectRoom> findRoomsByMember(@Param("member") Member member);

    /** 두 멤버 사이의 DM 방 조회 */
    @Query("SELECT drm1.room FROM DirectRoomMember drm1 " +
           "JOIN DirectRoomMember drm2 ON drm1.room = drm2.room " +
           "WHERE drm1.member = :a AND drm2.member = :b AND drm1.room.type = 'DM'")
    Optional<DirectRoom> findDmBetween(@Param("a") Member a, @Param("b") Member b);
}
