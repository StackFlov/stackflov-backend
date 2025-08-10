package com.stackflov.controller;

import com.stackflov.dto.*;
import com.stackflov.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // 사용자 목록 조회 API
    @GetMapping("/users")
    public ResponseEntity<Page<AdminUserDto>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<AdminUserDto> users = adminService.getUsers(page, size);
        return ResponseEntity.ok(users);
    }

    // 사용자 역할 변경 API
    @PutMapping("/users/{userId}/role")
    public ResponseEntity<String> updateUserRole(
            @PathVariable Long userId,
            @RequestBody RoleUpdateRequestDto dto,
            @RequestAttribute("email") String adminEmail) {
        adminService.updateUserRole(userId, dto, adminEmail);
        return ResponseEntity.ok("사용자 역할이 성공적으로 변경되었습니다.");
    }

    // 사용자 계정 상태 변경 API
    @PutMapping("/users/{userId}/status")
    public ResponseEntity<String> updateUserStatus(
            @PathVariable Long userId,
            @RequestBody UserStatUpdateRequestDto dto,
            @RequestAttribute("email") String adminEmail) {
        adminService.updateUserStatus(userId, dto, adminEmail);
        return ResponseEntity.ok("사용자 계정 상태가 성공적으로 변경되었습니다.");
    }
    @GetMapping("/reports/pending")
    public ResponseEntity<Page<AdminReportDto>> getPendingReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<AdminReportDto> reports = adminService.getPendingReports(page, size);
        return ResponseEntity.ok(reports);
    }

    // 신고 처리 API
    @PostMapping("/reports/{reportId}/process")
    public ResponseEntity<String> processReport(
            @PathVariable Long reportId,
            @RequestBody ReportProcessRequestDto dto,
            @RequestAttribute("email") String adminEmail) {
        adminService.processReport(reportId, dto, adminEmail);
        return ResponseEntity.ok("신고가 처리되었습니다.");
    }
}