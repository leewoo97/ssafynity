package com.ssafynity.demo.repository;

import com.ssafynity.demo.domain.Friendship;
import com.ssafynity.demo.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    /** 두 멤버 사이의 친구 관계 (방향 무관) */
    @Query("SELECT f FROM Friendship f WHERE " +
           "(f.requester = :a AND f.receiver = :b) OR (f.requester = :b AND f.receiver = :a)")
    Optional<Friendship> findBetween(@Param("a") Member a, @Param("b") Member b);

    /** member가 받은 PENDING 요청 목록 */
    List<Friendship> findByReceiverAndStatus(Member receiver, String status);

    /** member가 보낸 PENDING 요청 목록 */
    List<Friendship> findByRequesterAndStatus(Member requester, String status);

    /** member의 모든 ACCEPTED 친구 관계 */
    @Query("SELECT f FROM Friendship f WHERE " +
           "(f.requester = :m OR f.receiver = :m) AND f.status = 'ACCEPTED'")
    List<Friendship> findAcceptedFriendships(@Param("m") Member m);
}
