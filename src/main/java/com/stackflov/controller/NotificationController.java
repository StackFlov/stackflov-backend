package com.stackflov.controller;

import com.stackflov.config.CustomUserPrincipal;
import com.stackflov.dto.NotificationDto;
import com.stackflov.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // 내 알림 목록
    @GetMapping
    public ResponseEntity<Page<NotificationDto>> getMyNotifications(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            Pageable pageable) {
        Page<NotificationDto> page = notificationService.getMyNotifications(principal.getEmail(), pageable);
        return ResponseEntity.ok(page);
    }

    // 단건 읽음
    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markRead(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        notificationService.markRead(principal.getEmail(), id);
        return ResponseEntity.ok().build();
    }

    // 모두 읽음
    @PostMapping("/read-all")
    public ResponseEntity<Void> markAllRead(@AuthenticationPrincipal CustomUserPrincipal principal) {
        notificationService.markAllRead(principal.getEmail());
        return ResponseEntity.ok().build();
    }
}
