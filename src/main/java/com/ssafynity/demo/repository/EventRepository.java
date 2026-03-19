package com.ssafynity.demo.repository;

import com.ssafynity.demo.domain.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByStartDateAfterOrderByStartDateAsc(LocalDateTime now);
    List<Event> findTop4ByStartDateAfterOrderByStartDateAsc(LocalDateTime now);
    List<Event> findByStatusOrderByStartDateAsc(String status);
    List<Event> findAllByOrderByStartDateAsc();
}
