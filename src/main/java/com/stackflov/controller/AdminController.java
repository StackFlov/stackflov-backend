package com.stackflov.controller;

import com.stackflov.dto.*;
import com.stackflov.service.AdminService;
import com.stackflov.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final DashboardService dashboardService;

    @GetMapping("/users")
    public ResponseEntity<Page<AdminUserDto>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<AdminUserDto> users = adminService.getUsers(page, size);
        return ResponseEntity.ok(users);
    }

    @PutMapping("/users/{userId}/role")
    public ResponseEntity<String> updateUserRole(
            @PathVariable Long userId,
            @RequestBody RoleUpdateRequestDto dto,
            @AuthenticationPrincipal String adminEmail) {
        adminService.updateUserRole(userId, dto, adminEmail);
        return ResponseEntity.ok("사용자 역할이 성공적으로 변경되었습니다.");
    }

    @PutMapping("/users/{userId}/status")
    public ResponseEntity<String> updateUserStatus(
            @PathVariable Long userId,
            @RequestBody UserStatUpdateRequestDto dto,
            @AuthenticationPrincipal String adminEmail) {
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

    @PostMapping("/reports/{reportId}/process")
    public ResponseEntity<String> processReport(
            @PathVariable Long reportId,
            @RequestBody ReportProcessRequestDto dto,
            @AuthenticationPrincipal String adminEmail) {
        adminService.processReport(reportId, dto, adminEmail);
        return ResponseEntity.ok("신고가 처리되었습니다.");
    }

    @GetMapping("/dashboard/stats")
    public ResponseEntity<DashboardStatsDto> getDashboardStats() {
        DashboardStatsDto stats = dashboardService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/boards/search")
    public ResponseEntity<Page<AdminBoardDto>> searchBoards(
            @RequestParam String type,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<AdminBoardDto> result = adminService.searchBoardsByAdmin(type, keyword, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/comments/search")
    public ResponseEntity<Page<AdminCommentDto>> searchComments(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<AdminCommentDto> result = adminService.searchCommentsByAdmin(keyword, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/users/{userId}/boards")
    public ResponseEntity<Page<AdminBoardDto>> getBoardsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<AdminBoardDto> boards = adminService.getBoardsByUser(userId, pageable);
        return ResponseEntity.ok(boards);
    }

    @GetMapping("/users/{userId}/comments")
    public ResponseEntity<Page<AdminCommentDto>> getCommentsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<AdminCommentDto> comments = adminService.getCommentsByUser(userId, pageable);
        return ResponseEntity.ok(comments);
    }
}
