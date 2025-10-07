package com.stackflov.controller;

import com.stackflov.config.CustomUserPrincipal;
import com.stackflov.domain.BannedWord;
import com.stackflov.dto.*;
import com.stackflov.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Admin", description = "관리자 전용 API 모음")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final DashboardService dashboardService;
    private final BoardService boardService;
    private final BannedWordService bannedWordService;
    private final CommentService commentService;

    @Operation(
            summary = "관리자: 사용자 목록 조회",
            description = "페이지/사이즈로 사용자 목록을 조회합니다."
    )
    @GetMapping("/users")
    public ResponseEntity<Page<AdminUserDto>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<AdminUserDto> users = adminService.getUsers(page, size);
        return ResponseEntity.ok(users);
    }

    @Operation(
            summary = "관리자: 사용자 역할 변경",
            description = "지정한 사용자 ID의 역할(ROLE_USER/ROLE_ADMIN 등)을 변경합니다."
    )
    @PutMapping("/users/{userId}/role")
    public ResponseEntity<String> updateUserRole(
            @PathVariable Long userId,
            @RequestBody RoleUpdateRequestDto dto,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        adminService.updateUserRole(userId, dto, principal.getEmail());
        return ResponseEntity.ok("사용자 역할이 성공적으로 변경되었습니다.");
    }

    @Operation(
            summary = "관리자: 사용자 계정 상태 변경",
            description = "활성/비활성 등 사용자 상태를 변경합니다."
    )
    @PutMapping("/users/{userId}/status")
    public ResponseEntity<String> updateUserStatus(
            @PathVariable Long userId,
            @RequestBody UserStatUpdateRequestDto dto,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        adminService.updateUserStatus(userId, dto, principal.getEmail());
        return ResponseEntity.ok("사용자 계정 상태가 성공적으로 변경되었습니다.");
    }

    @Operation(
            summary = "관리자: 미처리 신고 목록 조회",
            description = "처리 대기 중인 신고를 페이지네이션으로 조회합니다."
    )
    @GetMapping("/reports/pending")
    public ResponseEntity<Page<AdminReportDto>> getPendingReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<AdminReportDto> reports = adminService.getPendingReports(page, size);
        return ResponseEntity.ok(reports);
    }

    @Operation(
            summary = "관리자: 신고 처리",
            description = "신고 ID에 대해 경고/삭제 등 처리 내용을 반영합니다."
    )
    @PostMapping("/reports/{reportId}/process")
    public ResponseEntity<String> processReport(
            @PathVariable Long reportId,
            @RequestBody ReportProcessRequestDto dto,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        adminService.processReport(reportId, dto, principal.getEmail());
        return ResponseEntity.ok("신고가 처리되었습니다.");
    }

    @Operation(
            summary = "관리자 대시보드: 요약 통계",
            description = "게시글/댓글/신고 등 핵심 지표의 요약 통계를 반환합니다."
    )
    @GetMapping("/dashboard/stats")
    public ResponseEntity<DashboardStatsDto> getDashboardStats() {
        DashboardStatsDto stats = dashboardService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }

    @Operation(
            summary = "관리자: 게시글 검색",
            description = "type(제목/내용/작성자 등), keyword 기준으로 게시글을 검색합니다."
    )
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

    @Operation(
            summary = "관리자: 댓글 검색",
            description = "keyword로 댓글 내용을 검색합니다."
    )
    @GetMapping("/comments/search")
    public ResponseEntity<Page<AdminCommentDto>> searchComments(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<AdminCommentDto> result = adminService.searchCommentsByAdmin(keyword, pageable);
        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "관리자: 특정 사용자의 게시글 조회",
            description = "userId가 작성한 게시글을 최신순으로 페이지네이션 조회합니다."
    )
    @GetMapping("/users/{userId}/boards")
    public ResponseEntity<Page<AdminBoardDto>> getBoardsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<AdminBoardDto> boards = adminService.getBoardsByUser(userId, pageable);
        return ResponseEntity.ok(boards);
    }

    @Operation(
            summary = "관리자: 특정 사용자의 댓글 조회",
            description = "userId가 작성한 댓글을 최신순으로 페이지네이션 조회합니다."
    )
    @GetMapping("/users/{userId}/comments")
    public ResponseEntity<Page<AdminCommentDto>> getCommentsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<AdminCommentDto> comments = adminService.getCommentsByUser(userId, pageable);
        return ResponseEntity.ok(comments);
    }

    @Operation(
            summary = "관리자: 게시글 비활성화(삭제 처리)",
            description = "게시글을 관리자 권한으로 비활성화합니다."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/boards/{boardId}")
    public ResponseEntity<?> deleteBoardByAdmin(@PathVariable Long boardId) {
        boardService.deactivateBoardByAdmin(boardId);
        return ResponseEntity.ok("삭제 완료");
    }
    @Operation(
            summary = "관리자: 댓글 비활성화",
            description = "댓글을 관리자 권한으로 비활성화합니다."
    )
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<String> deactivateCommentByAdmin(@PathVariable Long commentId) {
        commentService.deactivateCommentByAdmin(commentId);
        return ResponseEntity.ok("댓글이 성공적으로 비활성화되었습니다.");
    }

    @Operation(
            summary = "관리자: 콘텐츠 복구",
            description = "contentType(board/comment/review 등)과 contentId로 비활성화된 콘텐츠를 복구합니다."
    )
    @PutMapping("/content/{contentType}/{contentId}/reactivate")
    public ResponseEntity<String> reactivateContent(
            @PathVariable String contentType,
            @PathVariable Long contentId
    ) {
        adminService.reactivateContent(contentType, contentId);
        return ResponseEntity.ok("콘텐츠가 성공적으로 복구되었습니다.");
    }

    @Operation(
            summary = "관리자: 사용자 정지",
            description = """
                    userId 사용자를 지정 기간만큼 활동 정지합니다. <br/>
                    <b>기간 매핑(서버 시각 기준)</b>
                     <ul>
                         <li><code>THREE_DAYS</code> → 현재 시각 + 3일</li>
                         <li><code>SEVEN_DAYS</code> → 현재 시각 + 7일</li>
                         <li><code>TEN_DAYS</code> → 현재 시각 + 10일</li>
                         <li><code>THIRTY_DAYS</code> → 현재 시각 + 30일</li>
                         <li><code>SIX_MONTHS</code> → 현재 시각 + 6개월</li>
                         <li><code>PERMANENT</code> → 9999-12-31T23:59:59 로 고정(사실상 영구)</li>
                     </ul>
                    """
    )
    @PutMapping("/users/{userId}/suspend")
    public ResponseEntity<String> suspendUser(
            @PathVariable Long userId,
            @RequestBody UserSuspensionRequestDto dto) {
        adminService.suspendUser(userId, dto.getPeriod());
        return ResponseEntity.ok("사용자가 성공적으로 정지 처리되었습니다.");
    }

    @Operation(
            summary = "관리자: 사용자 메모 추가",
            description = "운영 메모를 사용자에게 추가하고 작성자 이메일을 기록합니다."
    )
    @PostMapping("/users/{userId}/memos")
    public ResponseEntity<AdminMemoResponseDto> addMemo(
            @PathVariable Long userId,
            @RequestBody AdminMemoRequestDto dto,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        AdminMemoResponseDto responseDto = adminService.addMemoToUser(userId, principal.getEmail(), dto);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(
            summary = "관리자: 사용자 메모 조회",
            description = "해당 사용자에 달린 운영 메모 목록을 반환합니다."
    )
    @GetMapping("/users/{userId}/memos")
    public ResponseEntity<List<AdminMemoResponseDto>> getMemos(@PathVariable Long userId) {
        List<AdminMemoResponseDto> memos = adminService.getMemosForUser(userId);
        return ResponseEntity.ok(memos);
    }

    @Operation(
            summary = "관리자: 게시글 일괄 비활성화",
            description = "전달된 ID 배열로 여러 게시글을 한 번에 비활성화합니다."
    )
    @PostMapping("/boards/bulk-deactivate")
    public ResponseEntity<String> bulkDeactivateBoards(@RequestBody BulkActionRequestDto dto) {
        adminService.bulkDeactivateBoards(dto.getIds());
        return ResponseEntity.ok("선택한 게시글들이 성공적으로 비활성화되었습니다.");
    }

    @Operation(
            summary = "관리자: 댓글 일괄 비활성화",
            description = "전달된 ID 배열로 여러 댓글을 한 번에 비활성화합니다."
    )
    @PostMapping("/comments/bulk-deactivate")
    public ResponseEntity<String> bulkDeactivateComments(@RequestBody BulkActionRequestDto dto) {
        adminService.bulkDeactivateComments(dto.getIds());
        return ResponseEntity.ok("선택한 댓글들이 성공적으로 비활성화되었습니다.");
    }

    @Operation(
            summary = "관리자: 리뷰 비활성화",
            description = "리뷰를 관리자 권한으로 비활성화합니다."
    )
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<String> deactivateReviewByAdmin(@PathVariable Long reviewId) {
        adminService.deactivateReviewByAdmin(reviewId);
        return ResponseEntity.ok("리뷰가 성공적으로 비활성화되었습니다.");
    }

    @Operation(
            summary = "관리자 대시보드: 상세 통계",
            description = "기간/지표별 상세 통계를 반환합니다."
    )
    @GetMapping("/dashboard/detailed-stats")
    public ResponseEntity<DetailedStatsDto> getDetailedStats() {
        DetailedStatsDto stats = dashboardService.getDetailedStats();
        return ResponseEntity.ok(stats);
    }

    // --- 금칙어 관리 API ---
    @Operation(
            summary = "관리자: 금칙어 전체 조회",
            description = "등록된 금칙어 전체 목록을 조회합니다."
    )
    @GetMapping("/banned-words")
    public ResponseEntity<List<BannedWord>> getAllBannedWords() {
        return ResponseEntity.ok(bannedWordService.getAllBannedWords());
    }

    @Operation(
            summary = "관리자: 금칙어 추가",
            description = "요청 바디의 word 값을 금칙어로 추가합니다."
    )
    @PostMapping("/banned-words")
    public ResponseEntity<BannedWord> addBannedWord(@RequestBody BannedWordRequestDto dto) {
        BannedWord newWord = bannedWordService.addBannedWord(dto.getWord());
        return ResponseEntity.ok(newWord);
    }

    @Operation(
            summary = "관리자: 금칙어 삭제",
            description = "요청 바디의 word 값을 금칙어 목록에서 삭제합니다."
    )
    @DeleteMapping("/banned-words")
    public ResponseEntity<Void> deleteBannedWord(@RequestBody BannedWordRequestDto dto) {
        bannedWordService.deleteBannedWord(dto.getWord());
        return ResponseEntity.noContent().build();
    }
}
// 무중단 배포 테스트
