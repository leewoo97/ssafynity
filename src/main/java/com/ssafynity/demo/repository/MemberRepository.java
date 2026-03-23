package com.ssafynity.demo.repository;

import com.ssafynity.demo.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByUsername(String username);
    List<Member> findByCampusAndCohortOrderByNicknameAsc(String campus, Integer cohort);
    List<Member> findByCampusOrderByNicknameAsc(String campus);
    List<Member> findByCampusAndCohortAndClassCodeOrderByNicknameAsc(String campus, Integer cohort, Integer classCode);
}
