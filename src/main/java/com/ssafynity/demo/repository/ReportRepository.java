package com.ssafynity.demo.repository;

import com.ssafynity.demo.domain.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByResolvedFalseOrderByCreatedAtDesc();
}
