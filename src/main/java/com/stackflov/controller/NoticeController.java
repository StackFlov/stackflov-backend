package com.stackflov.controller;

import com.stackflov.config.CustomUserPrincipal;
import com.stackflov.dto.NoticeCreateRequestDto;
import com.stackflov.dto.NoticeResponseDto;
import com.stackflov.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    //-- 사용자용 API --//

    @GetMapping("/notices")
    public ResponseEntity<Page<NoticeResponseDto>> getAllNotices(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(noticeService.getAllNotices(pageable));
    }

    @GetMapping("/notices/{noticeId}")
    public ResponseEntity<NoticeResponseDto> getNotice(@PathVariable Long noticeId) {
        return ResponseEntity.ok(noticeService.getNotice(noticeId));
    }

    //-- 관리자용 API --//

    @PostMapping("/admin/notices")
    public ResponseEntity<Long> createNotice(@RequestBody NoticeCreateRequestDto dto,
                                             @AuthenticationPrincipal CustomUserPrincipal principal) {
        Long noticeId = noticeService.createNotice(dto, principal.getEmail());
        return ResponseEntity.ok(noticeId);
    }

    @PutMapping("/admin/notices/{noticeId}")
    public ResponseEntity<Void> updateNotice(@PathVariable Long noticeId,
                                             @RequestBody NoticeCreateRequestDto dto) {
        noticeService.updateNotice(noticeId, dto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/admin/notices/{noticeId}")
    public ResponseEntity<Void> deleteNotice(@PathVariable Long noticeId) {
        noticeService.deleteNotice(noticeId);
        return ResponseEntity.noContent().build();
    }
}