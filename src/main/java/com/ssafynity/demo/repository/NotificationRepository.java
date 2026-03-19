package com.ssafynity.demo.repository;

import com.ssafynity.demo.domain.Notification;
import com.ssafynity.demo.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByReceiverOrderByCreatedAtDesc(Member receiver);
    int countByReceiverAndIsReadFalse(Member receiver);
}
