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
    private final Long reportedUserId;
    private final String reportedUserNickname;
    private final int reportedUserReportCount;
    private final Long parentBoardId;     // 댓글이 속한 게시글 ID (게시글 댓글)
    private final Long parentReviewId;    // 댓글이 속한 리뷰 ID (리뷰 댓글)
    private final String parentType;      // "BOARD" | "REVIEW" | null
    private final String contentUrl;
    private final String contentExcerpt;


    public AdminReportDto(
            Report report, User reportedUser,
            Long parentBoardId, Long parentReviewId, String parentType,
            String contentUrl, String contentExcerpt
    ) {
        this.reportId = report.getId();
        this.contentId = report.getContentId();
        this.contentType = report.getContentType();
        this.reason = report.getReason();
        this.details = report.getDetails();
        this.status = report.getStatus();
        this.reporterNickname = report.getReporter().getNickname();
        this.createdAt = report.getCreatedAt();
        this.reportedUserId = reportedUser.getId();
        this.reportedUserNickname = reportedUser.getNickname();
        this.reportedUserReportCount = reportedUser.getReportCount();

        this.parentBoardId = parentBoardId;
        this.parentReviewId = parentReviewId;
        this.parentType = parentType;
        this.contentUrl = contentUrl;
        this.contentExcerpt = contentExcerpt;
    }
}