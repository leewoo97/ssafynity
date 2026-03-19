package com.ssafynity.demo.service;

import com.ssafynity.demo.domain.Member;
import com.ssafynity.demo.domain.Report;
import com.ssafynity.demo.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;

    @Transactional
    public void report(Member reporter, String targetType, Long targetId, String reason) {
        Report report = Report.builder()
                .reporter(reporter)
                .targetType(targetType)
                .targetId(targetId)
                .reason(reason)
                .build();
        reportRepository.save(report);
    }

    public List<Report> findPending() {
        return reportRepository.findByResolvedFalseOrderByCreatedAtDesc();
    }

    @Transactional
    public void resolve(Long id) {
        Report report = reportRepository.findById(id).orElseThrow();
        report.setResolved(true);
    }
}
