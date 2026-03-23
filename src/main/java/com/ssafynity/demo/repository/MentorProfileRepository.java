package com.ssafynity.demo.repository;

import com.ssafynity.demo.domain.Member;
import com.ssafynity.demo.domain.MentorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MentorProfileRepository extends JpaRepository<MentorProfile, Long> {

    Optional<MentorProfile> findByMember(Member member);

    boolean existsByMember(Member member);

    List<MentorProfile> findByActiveTrueOrderByCreatedAtDesc();

    List<MentorProfile> findBySpecialtiesContainingIgnoreCaseAndActiveTrue(String keyword);
}
