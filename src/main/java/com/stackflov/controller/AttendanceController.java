package com.stackflov.controller;

import com.stackflov.config.CustomUserPrincipal; // ✅ 커스텀 클래스 임포트
import com.stackflov.domain.User;
import com.stackflov.repository.UserRepository;
import com.stackflov.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final UserRepository userRepository;

    @PostMapping("/check-in")
    public ResponseEntity<String> dailyCheckIn(@AuthenticationPrincipal CustomUserPrincipal principal) {
        // 1. 커스텀 Principal에서 안전하게 ID를 꺼내 유저 조회
        // principal.getId() 덕분에 DB 인덱스 조회가 더 빨라집니다.
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        try {
            // 2. 출석 서비스 실행
            String result = attendanceService.checkIn(user);
            return ResponseEntity.ok(result);
        } catch (IllegalStateException e) {
            // 이미 출석한 경우 등에 대한 예외 처리
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
