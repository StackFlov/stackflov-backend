package com.stackflov.controller;

import com.stackflov.config.CustomUserPrincipal;
import com.stackflov.domain.BannedWord;
import com.stackflov.dto.*;
import com.stackflov.service.AdminService;
import com.stackflov.service.BannedWordService;
import com.stackflov.service.BoardService;
import com.stackflov.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final DashboardService dashboardService;
    private final BoardService boardService;
    private final BannedWordService bannedWordService;

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
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        adminService.updateUserRole(userId, dto, principal.getEmail());
        return ResponseEntity.ok("사용자 역할이 성공적으로 변경되었습니다.");
    }

    @PutMapping("/users/{userId}/status")
    public ResponseEntity<String> updateUserStatus(
            @PathVariable Long userId,
            @RequestBody UserStatUpdateRequestDto dto,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        adminService.updateUserStatus(userId, dto, principal.getEmail());
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
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{boardId}")
    public ResponseEntity<?> deleteBoardByAdmin(@PathVariable Long boardId) {
        boardService.deactivateBoardByAdmin(boardId);
        return ResponseEntity.ok("삭제 완료");
    }

    @PutMapping("/content/{contentType}/{contentId}/reactivate")
    public ResponseEntity<String> reactivateContent(
            @PathVariable String contentType,
            @PathVariable Long contentId
    ) {
        adminService.reactivateContent(contentType, contentId);
        return ResponseEntity.ok("콘텐츠가 성공적으로 복구되었습니다.");
    }

    @PutMapping("/users/{userId}/suspend")
    public ResponseEntity<String> suspendUser(
            @PathVariable Long userId,
            @RequestBody UserSuspensionRequestDto dto) {
        adminService.suspendUser(userId, dto.getPeriod());
        return ResponseEntity.ok("사용자가 성공적으로 정지 처리되었습니다.");
    }

    @PostMapping("/users/{userId}/memos")
    public ResponseEntity<AdminMemoResponseDto> addMemo(
            @PathVariable Long userId,
            @RequestBody AdminMemoRequestDto dto,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        AdminMemoResponseDto responseDto = adminService.addMemoToUser(userId, principal.getEmail(), dto);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/users/{userId}/memos")
    public ResponseEntity<List<AdminMemoResponseDto>> getMemos(@PathVariable Long userId) {
        List<AdminMemoResponseDto> memos = adminService.getMemosForUser(userId);
        return ResponseEntity.ok(memos);
    }

    @PostMapping("/boards/bulk-deactivate")
    public ResponseEntity<String> bulkDeactivateBoards(@RequestBody BulkActionRequestDto dto) {
        adminService.bulkDeactivateBoards(dto.getIds());
        return ResponseEntity.ok("선택한 게시글들이 성공적으로 비활성화되었습니다.");
    }

    @PostMapping("/comments/bulk-deactivate")
    public ResponseEntity<String> bulkDeactivateComments(@RequestBody BulkActionRequestDto dto) {
        adminService.bulkDeactivateComments(dto.getIds());
        return ResponseEntity.ok("선택한 댓글들이 성공적으로 비활성화되었습니다.");
    }

    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<String> deactivateReviewByAdmin(@PathVariable Long reviewId) {
        adminService.deactivateReviewByAdmin(reviewId);
        return ResponseEntity.ok("리뷰가 성공적으로 비활성화되었습니다.");
    }

    @GetMapping("/dashboard/detailed-stats")
    public ResponseEntity<DetailedStatsDto> getDetailedStats() {
        DetailedStatsDto stats = dashboardService.getDetailedStats();
        return ResponseEntity.ok(stats);
    }

    // --- 금칙어 관리 API ---
    @GetMapping("/banned-words")
    public ResponseEntity<List<BannedWord>> getAllBannedWords() {
        return ResponseEntity.ok(bannedWordService.getAllBannedWords());
    }

    @PostMapping("/banned-words")
    public ResponseEntity<BannedWord> addBannedWord(@RequestBody BannedWordRequestDto dto) {
        BannedWord newWord = bannedWordService.addBannedWord(dto.getWord());
        return ResponseEntity.ok(newWord);
    }

    @DeleteMapping("/banned-words")
    public ResponseEntity<Void> deleteBannedWord(@RequestBody BannedWordRequestDto dto) {
        bannedWordService.deleteBannedWord(dto.getWord());
        return ResponseEntity.noContent().build();
    }
}
