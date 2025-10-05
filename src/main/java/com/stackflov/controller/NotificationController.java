package com.stackflov.controller;

import com.stackflov.config.CustomUserPrincipal;
import com.stackflov.dto.NotificationDto;
import com.stackflov.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Notifications", description = "알림 조회 및 읽음 처리 API")
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // 내 알림 목록
    @Operation(summary = "내 알림 목록 조회", description = "로그인한 사용자의 알림을 페이징으로 조회합니다.")
    @GetMapping
    public ResponseEntity<Page<NotificationDto>> getMyNotifications(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            Pageable pageable) {
        Page<NotificationDto> page = notificationService.getMyNotifications(principal.getEmail(), pageable);
        return ResponseEntity.ok(page);
    }

    // 단건 읽음
    @Operation(summary = "알림 단건 읽음 처리", description = "알림 ID에 해당하는 알림을 읽음으로 표시합니다.")
    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markRead(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        notificationService.markRead(principal.getEmail(), id);
        return ResponseEntity.ok().build();
    }

    // 모두 읽음
    @Operation(summary = "알림 전체 읽음 처리", description = "내 알림 전체를 읽음으로 표시합니다.")
    @PostMapping("/read-all")
    public ResponseEntity<Void> markAllRead(@AuthenticationPrincipal CustomUserPrincipal principal) {
        notificationService.markAllRead(principal.getEmail());
        return ResponseEntity.ok().build();
    }
}
