package com.stackflov.controller;

import com.stackflov.config.CustomUserPrincipal;
import com.stackflov.dto.NoticeCreateRequestDto;
import com.stackflov.dto.NoticeResponseDto;
import com.stackflov.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Notices", description = "공지사항 조회 API")
@RestController
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    //-- 사용자용 API --//

    @Operation(summary = "공지 목록 조회", description = "공지사항을 페이지네이션으로 조회합니다.")
    @GetMapping("/notices")
    public ResponseEntity<Page<NoticeResponseDto>> getAllNotices(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(noticeService.getAllNotices(pageable));
    }

    @Operation(summary = "공지 상세 조회", description = "noticeId에 해당하는 공지사항을 조회합니다.")
    @GetMapping("/notices/{noticeId}")
    public ResponseEntity<NoticeResponseDto> getNotice(@PathVariable Long noticeId) {
        return ResponseEntity.ok(noticeService.getNotice(noticeId));
    }

    //-- 관리자용 API --//

    @Operation(tags = {"Admin Notices"}, summary = "공지 생성", description = "관리자가 새로운 공지사항을 생성합니다.")
    @PostMapping("/admin/notices")
    public ResponseEntity<Long> createNotice(@RequestBody NoticeCreateRequestDto dto,
                                             @AuthenticationPrincipal CustomUserPrincipal principal) {
        Long noticeId = noticeService.createNotice(dto, principal.getEmail());
        return ResponseEntity.ok(noticeId);
    }

    @Operation(tags = {"Admin Notices"}, summary = "공지 수정", description = "관리자가 기존 공지사항을 수정합니다.")
    @PutMapping("/admin/notices/{noticeId}")
    public ResponseEntity<Void> updateNotice(@PathVariable Long noticeId,
                                             @RequestBody NoticeCreateRequestDto dto) {
        noticeService.updateNotice(noticeId, dto);
        return ResponseEntity.ok().build();
    }

    @Operation(tags = {"Admin Notices"}, summary = "공지 삭제", description = "관리자가 공지사항을 삭제합니다.")
    @DeleteMapping("/admin/notices/{noticeId}")
    public ResponseEntity<Void> deleteNotice(@PathVariable Long noticeId) {
        noticeService.deleteNotice(noticeId);
        return ResponseEntity.noContent().build();
    }
}