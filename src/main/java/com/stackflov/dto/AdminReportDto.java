package com.stackflov.dto;

import com.stackflov.domain.*;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class AdminReportDto {
    private final Long reportId;
    private final Long contentId;
    private final ReportType contentType;
    private final ReportReason reason;
    private final String details;
    private final ReportStatus status;
    private final String reporterNickname;
    private final LocalDateTime createdAt;

    public AdminReportDto(Report report) {
        this.reportId = report.getId();
        this.contentId = report.getContentId();
        this.contentType = report.getContentType();
        this.reason = report.getReason();
        this.details = report.getDetails();
        this.status = report.getStatus();
        this.reporterNickname = report.getReporter().getNickname();
        this.createdAt = report.getCreatedAt();
    }
}