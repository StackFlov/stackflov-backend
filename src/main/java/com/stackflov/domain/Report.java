package com.stackflov.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "reports", uniqueConstraints = {
        // 한 사용자가 동일한 콘텐츠를 중복 신고하지 못하도록 제약조건 추가
        @UniqueConstraint(columnNames = {"reporter_id", "contentId", "contentType"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long id;

    // 신고한 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    // 신고된 콘텐츠의 ID (게시글 ID 또는 댓글 ID)
    @Column(nullable = false)
    private Long contentId;

    // 신고된 콘텐츠의 종류 (게시글, 댓글 등)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportType contentType;

    // 신고 사유
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportReason reason;

    // 상세 사유 (선택 사항)
    @Lob
    private String details;

    // 신고 처리 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status;

    @CreationTimestamp
    private LocalDateTime createdAt;
}