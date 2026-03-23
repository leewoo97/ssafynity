package com.ssafynity.demo.repository;

import com.ssafynity.demo.domain.Member;
import com.ssafynity.demo.domain.MentoringRequest;
import com.ssafynity.demo.domain.MentorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MentoringRequestRepository extends JpaRepository<MentoringRequest, Long> {

    List<MentoringRequest> findByMenteeOrderByCreatedAtDesc(Member mentee);

    List<MentoringRequest> findByMentorProfileOrderByCreatedAtDesc(MentorProfile mentorProfile);

    List<MentoringRequest> findByMentorProfileAndStatusOrderByCreatedAtDesc(MentorProfile mentorProfile, String status);

    boolean existsByMenteeAndMentorProfileAndStatusIn(Member mentee, MentorProfile mentorProfile, List<String> statuses);

    long countByMentorProfileAndStatus(MentorProfile mentorProfile, String status);
}
