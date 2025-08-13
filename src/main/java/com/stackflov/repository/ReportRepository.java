package com.stackflov.repository;

import com.stackflov.domain.Report;
import com.stackflov.domain.ReportStatus;
import com.stackflov.domain.ReportType;
import com.stackflov.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
    // 중복 신고 확인을 위한 메서드
    boolean existsByReporterAndContentIdAndContentType(User reporter, Long contentId, ReportType contentType);
    //특정 상태의 신고 목록을 페이징하여 조회
    Page<Report> findByStatus(ReportStatus status, Pageable pageable);

    long countByStatus(ReportStatus status);
}