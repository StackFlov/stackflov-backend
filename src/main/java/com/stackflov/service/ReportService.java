package com.stackflov.service;

import com.stackflov.domain.*;
import com.stackflov.dto.ReportRequestDto;
import com.stackflov.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private static final String MSG_MISSING_REQUIRED = "필수 항목이 누락되었습니다.";
    private static final String MSG_OTHER_NEED_DETAILS = "기타 사유는 상세 내용을 입력해야 합니다.";
    private static final String MSG_SELF_REPORT_BLOCKED = "본인 콘텐츠는 신고할 수 없습니다.";
    private static final String MSG_DUP_REPORT = "이미 신고한 콘텐츠입니다.";
    private static final int    DETAILS_MAX_LEN = 2000;

    private final ReportRepository reportRepository;
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;
    private final ReviewRepository reviewRepository;
    private final UserService userService;

    @Transactional
    public void createReport(final String reporterEmail, final ReportRequestDto dto) {
        // 0) 기본 검증 (컨트롤러 @Valid와 중복되지만 방어적으로 유지)
        if (dto.getContentId() == null || dto.getContentType() == null || dto.getReason() == null) {
            throw new IllegalArgumentException(MSG_MISSING_REQUIRED);
        }
        if (dto.getReason() == ReportReason.OTHER &&
                (dto.getDetails() == null || dto.getDetails().isBlank())) {
            throw new IllegalArgumentException(MSG_OTHER_NEED_DETAILS);
        }

        final User reporter = userService.getValidUserByEmail(reporterEmail);

        // 1) 대상 존재 & 활성 체크 + 자기 신고 차단
        final TargetRef target = loadActiveTargetOrThrow(dto.getContentId(), dto.getContentType());
        if (target.authorId() != null && target.authorId().equals(reporter.getId())) {
            throw new IllegalArgumentException(MSG_SELF_REPORT_BLOCKED);
        }

        // 2) 애플리케이션 레벨 중복 차단 (빠른 경로)
        final boolean already = reportRepository
                .existsByReporterAndContentIdAndContentType(reporter, dto.getContentId(), dto.getContentType());
        if (already) {
            throw new IllegalStateException(MSG_DUP_REPORT);
        }

        // 3) 입력 정리
        final String safeDetails = sanitizeAndTrim(dto.getDetails(), DETAILS_MAX_LEN);

        final Report report = Report.builder()
                .reporter(reporter)
                .contentId(dto.getContentId())
                .contentType(dto.getContentType())
                .reason(dto.getReason())
                .details(safeDetails)
                .status(ReportStatus.PENDING)
                .build();

        // 4) 경쟁조건 방어 — DB 유니크 제약 충돌 메시지 통일
        try {
            reportRepository.save(report);
        } catch (DataIntegrityViolationException dup) {
            throw new IllegalStateException(MSG_DUP_REPORT);
        }
    }

    /** 활성(soft delete 아님) 대상만 허용하고, 작성자 ID를 함께 반환. */
    private TargetRef loadActiveTargetOrThrow(final Long contentId, final ReportType type) {
        return switch (type) {
            case BOARD -> boardRepository.findByIdAndActiveTrue(contentId)
                    .map(b -> new TargetRef(b.getId(), b.getAuthor() != null ? b.getAuthor().getId() : null))
                    .orElseThrow(() -> new IllegalArgumentException("신고할 게시글이 존재하지 않거나 비활성 상태입니다."));
            case COMMENT -> commentRepository.findByIdAndActiveTrue(contentId)
                    .map(c -> new TargetRef(c.getId(), c.getUser() != null ? c.getUser().getId() : null))
                    .orElseThrow(() -> new IllegalArgumentException("신고할 댓글이 존재하지 않거나 비활성 상태입니다."));
            case REVIEW -> reviewRepository.findById(contentId) // @Where(active=true) 가정
                    .map(r -> new TargetRef(r.getId(), r.getAuthor() != null ? r.getAuthor().getId() : null))
                    .orElseThrow(() -> new IllegalArgumentException("신고할 리뷰가 존재하지 않거나 비활성 상태입니다."));
        };
    }

    private String sanitizeAndTrim(final String s, final int maxLen) {
        if (s == null || s.isBlank()) return null;
        final String escaped = HtmlUtils.htmlEscape(s);
        return escaped.length() > maxLen ? escaped.substring(0, maxLen) : escaped;
    }

    /** 대상 식별 + 작성자 식별을 함께 넘기는 소형 레코드 */
    private record TargetRef(Long id, Long authorId) {}
}
