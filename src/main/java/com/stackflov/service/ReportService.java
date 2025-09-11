package com.stackflov.service;

import com.stackflov.domain.*;
import com.stackflov.dto.ReportRequestDto;
import com.stackflov.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;
    private final ReviewRepository reviewRepository;

    @Transactional
    public void createReport(String reporterEmail, ReportRequestDto dto) {
        User reporter = userRepository.findByEmail(reporterEmail)
                .orElseThrow(() -> new IllegalArgumentException("신고자를 찾을 수 없습니다."));

        // 신고 대상 콘텐츠가 실제로 존재하는지 확인
        validateContentExists(dto.getContentId(), dto.getContentType());

        // 동일한 콘텐츠를 이미 신고했는지 확인
        if (reportRepository.existsByReporterAndContentIdAndContentType(reporter, dto.getContentId(), dto.getContentType())) {
            throw new IllegalStateException("이미 신고한 콘텐츠입니다.");
        }

        Report report = Report.builder()
                .reporter(reporter)
                .contentId(dto.getContentId())
                .contentType(dto.getContentType())
                .reason(dto.getReason())
                .details(dto.getDetails())
                .status(ReportStatus.PENDING) // 초기 상태는 '접수 대기'
                .build();

        reportRepository.save(report);
    }

    private void validateContentExists(Long contentId, ReportType contentType) {
        switch (contentType) {
            case BOARD:
                boardRepository.findById(contentId)
                        .orElseThrow(() -> new IllegalArgumentException("신고할 게시글이 존재하지 않습니다."));
                break;
            case COMMENT:
                commentRepository.findById(contentId)
                        .orElseThrow(() -> new IllegalArgumentException("신고할 댓글이 존재하지 않습니다."));
                break;
            case REVIEW:
                reviewRepository.findById(contentId)
                        .orElseThrow(() -> new IllegalArgumentException("신고할 리뷰가 존재하지 않습니다."));
                break;
            default:
                throw new IllegalArgumentException("알 수 없는 콘텐츠 타입입니다.");
        }
    }
}