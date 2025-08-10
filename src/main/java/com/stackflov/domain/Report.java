package com.stackflov.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "reports", uniqueConstraints = {
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @Column(nullable = false)
    private Long contentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportType contentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportReason reason;

    @Lob
    private String details;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status;

    @CreationTimestamp
    private LocalDateTime createdAt;

    // --- 아래 필드들을 추가합니다 ---

    // 신고를 처리한 관리자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processor_id")
    private User processor;

    // 관리자 메모
    @Lob
    private String adminComment;

    // 처리 일시 (상태 변경 시 자동으로 업데이트됨)
    @UpdateTimestamp
    private LocalDateTime processedAt;

    // === 신고 처리 비즈니스 메서드 추가 ===
    public void process(User processor, ReportStatus status, String adminComment) {
        if (this.status != ReportStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 신고입니다.");
        }
        this.processor = processor;
        this.status = status;
        this.adminComment = adminComment;
    }
}