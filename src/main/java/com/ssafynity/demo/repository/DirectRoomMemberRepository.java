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

    @Modifying
    @Transactional
    @Query("DELETE FROM DirectRoomMember drm WHERE drm.room = :room AND drm.member = :member")
    void deleteByRoomAndMember(@Param("room") DirectRoom room, @Param("member") Member member);
}
