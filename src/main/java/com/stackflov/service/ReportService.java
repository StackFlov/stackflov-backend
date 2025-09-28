package com.stackflov.service;

import com.stackflov.domain.*;
import com.stackflov.dto.ReportRequestDto;
import com.stackflov.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;
    private final ReviewRepository reviewRepository;
    private final UserService userService;

    @Transactional
    public void createReport(String reporterEmail, ReportRequestDto dto) {
        // 0) 기본 검증 (컨트롤러 @Valid와 중복되지만 방어적으로)
        if (dto.getContentId() == null || dto.getContentType() == null || dto.getReason() == null) {
            throw new IllegalArgumentException("필수 항목이 누락되었습니다.");
        }
        if (dto.getReason() == ReportReason.OTHER &&
                (dto.getDetails() == null || dto.getDetails().isBlank())) {
            throw new IllegalArgumentException("기타 사유는 상세 내용을 입력해야 합니다.");
        }

        User reporter = userService.getValidUserByEmail(reporterEmail);

        // 1) 대상 존재 & 활성 체크 + 자기 신고 차단
        TargetRef target = loadActiveTargetOrThrow(dto.getContentId(), dto.getContentType());
        if (target.authorId() != null && target.authorId().equals(reporter.getId())) {
            throw new IllegalArgumentException("본인 콘텐츠는 신고할 수 없습니다.");
        }

        // 2) 중복 신고(애플리케이션 레벨) — 빠른 차단
        boolean already = reportRepository
                .existsByReporterAndContentIdAndContentType(reporter, dto.getContentId(), dto.getContentType());
        if (already) {
            throw new IllegalStateException("이미 신고한 콘텐츠입니다.");
        }

        // 3) 입력 정리 (상세 길이 제한 + 간단 무해화)
        String safeDetails = sanitizeAndTrim(dto.getDetails(), 2000); // 2KB 정도로 제한 (필요 시 조정)

        Report report = Report.builder()
                .reporter(reporter)
                .contentId(dto.getContentId())
                .contentType(dto.getContentType())
                .reason(dto.getReason())
                .details(safeDetails)
                .status(ReportStatus.PENDING)
                .build();

        // 4) 경쟁조건 방어 — DB 유니크 제약 충돌을 사용자 친화 에러로 변환
        try {
            reportRepository.save(report);
        } catch (DataIntegrityViolationException dup) {
            // @UniqueConstraint(reporter_id, contentId, contentType) 충돌
            throw new IllegalStateException("이미 신고한 콘텐츠입니다.");
        }
    }

    /**
     * 활성(soft delete 아님)인 대상만 허용하고, 작성자 ID를 함께 반환.
     */
    private TargetRef loadActiveTargetOrThrow(Long contentId, ReportType type) {
        return switch (type) {
            case BOARD -> boardRepository.findByIdAndActiveTrue(contentId)
                    .map(b -> new TargetRef(b.getId(), b.getAuthor() != null ? b.getAuthor().getId() : null))
                    .orElseThrow(() -> new IllegalArgumentException("신고할 게시글이 존재하지 않거나 비활성 상태입니다."));

            case COMMENT -> commentRepository.findByIdAndActiveTrue(contentId)
                    .map(c -> new TargetRef(c.getId(), c.getUser() != null ? c.getUser().getId() : null))
                    .orElseThrow(() -> new IllegalArgumentException("신고할 댓글이 존재하지 않거나 비활성 상태입니다."));

            case REVIEW -> reviewRepository.findById(contentId) // ✅ @Where 로 active=true 자동 필터
                    .map(r -> new TargetRef(r.getId(), r.getAuthor() != null ? r.getAuthor().getId() : null)) // ✅ getAuthor()
                    .orElseThrow(() -> new IllegalArgumentException("신고할 리뷰가 존재하지 않거나 비활성 상태입니다."));
        };
    }

    private String sanitizeAndTrim(String s, int maxLen) {
        if (s == null) return null;
        String trimmed = s.length() > maxLen ? s.substring(0, maxLen) : s;
        // 아주 기본적인 태그 이스케이프 (서버 전역 Sanitizer가 있다면 그걸 사용)
        return trimmed
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    /** 대상 식별 + 작성자 식별을 함께 넘기는 소형 레코드 */
    private record TargetRef(Long id, Long authorId) {}
}
