package com.stackflov.controller;

import com.stackflov.config.CustomUserPrincipal;
import com.stackflov.dto.ReportRequestDto;
import com.stackflov.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Reports", description = "신고 생성 API")
@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @Operation(
            summary = "신고 생성",
            description = """
        콘텐츠(게시글/댓글/리뷰)를 신고합니다.

        [contentType]
        - BOARD: 게시글
        - COMMENT: 댓글
        - REVIEW: 리뷰

        [reason]
        - SPAM: 스팸/광고
        - ABUSE: 욕설/비방
        - PORNOGRAPHY: 음란물
        - ILLEGAL: 불법 정보
        - OTHER: 기타 (※ details 필수)

        [필드 규칙]
        - contentId: 신고 대상의 ID (필수)
        - contentType: BOARD | COMMENT | REVIEW (필수)
        - reason: SPAM | ABUSE | PORNOGRAPHY | ILLEGAL | OTHER (필수)
        - details: OTHER일 때만 필수, 그 외 사유는 선택
        """
    )
    @PostMapping
    public ResponseEntity<String> createReport(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestBody ReportRequestDto dto) {

        reportService.createReport(principal.getEmail(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body("신고가 정상적으로 접수되었습니다.");
    }
}
