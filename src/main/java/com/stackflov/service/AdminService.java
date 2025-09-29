package com.stackflov.service;

import com.stackflov.domain.*;
import com.stackflov.dto.*;
import com.stackflov.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final ReportRepository reportRepository;
    private final BoardService boardService;
    private final CommentService commentService;
    private final BoardRepository boardRepository; // 추가
    private final CommentRepository commentRepository;
    private final BookmarkService bookmarkService;
    private final FollowService followService;
    private final NotificationService notificationService;
    private final MapService mapService;
    private final ReviewRepository reviewRepository;
    private final AdminNoteRepository adminNoteRepository;

    // 모든 사용자 목록 조회
    @Transactional(readOnly = true)
    public Page<AdminUserDto> getUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<User> users = userRepository.findAll(pageable);
        return users.map(AdminUserDto::new);
    }

    // 사용자 역할 변경
    @Transactional
    public void updateUserRole(Long userId, RoleUpdateRequestDto dto, String adminEmail) {
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (targetUser.getEmail().equals(adminEmail)) {
            throw new IllegalArgumentException("자신의 역할은 변경할 수 없습니다.");
        }

        if (targetUser.getRole() == Role.ADMIN && userRepository.countByRole(Role.ADMIN) <= 1) {
            throw new IllegalStateException("마지막 남은 관리자의 역할은 변경할 수 없습니다.");
        }

        targetUser.updateRole(dto.getRole());
    }

    @Transactional
    public void updateUserStatus(Long userId, UserStatUpdateRequestDto dto, String adminEmail) {
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // ... 기존의 자기 자신, 마지막 관리자 보호 로직 ...
        if (targetUser.getEmail().equals(adminEmail)) {
            throw new IllegalArgumentException("자신의 계정 상태는 변경할 수 없습니다.");
        }
        if (targetUser.getRole() == Role.ADMIN && !dto.isActive() && userRepository.countByRole(Role.ADMIN) <= 1) {
            throw new IllegalStateException("마지막 남은 관리자는 비활성화할 수 없습니다.");
        }

        targetUser.updateStatus(dto.isActive());

        if (!dto.isActive()) {
            commentService.deactivateAllCommentsByUser(targetUser);
            boardService.deactivateAllBoardsByUser(targetUser);
            bookmarkService.deactivateAllBookmarksByUser(targetUser);
            followService.deactivateAllFollowsByUser(targetUser);
        }
    }

    @Transactional(readOnly = true)
    public Page<AdminReportDto> getPendingReports(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Report> reports = reportRepository.findByStatus(ReportStatus.PENDING, pageable);
        return reports.map(this::convertReportToAdminDto);
    }

    // 신고 처리
    @Transactional
    public void processReport(Long reportId, ReportProcessRequestDto dto, String adminEmail) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new IllegalArgumentException("관리자 계정을 찾을 수 없습니다."));

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("신고 내역을 찾을 수 없습니다."));

        // 신고 내역 상태 변경
        report.process(admin, dto.getStatus(), dto.getAdminComment());

        // 신고 승인(REVIEWED) 시, 해당 콘텐츠 삭제
        if (dto.getStatus() == ReportStatus.REVIEWED) {
            deleteReportedContent(report.getContentId(), report.getContentType());
        }
        notificationService.notify(
                report.getReporter(),
                NotificationType.REPORT,
                "신고 #" + report.getId() + " 처리 결과: " + dto.getStatus(),
                "/admin/reports/" + report.getId()
        );
    }

    private void deleteReportedContent(Long contentId, ReportType contentType) {
        if (contentType == ReportType.BOARD) {
            boardService.deactivateBoardByAdmin(contentId);
        } else if (contentType == ReportType.COMMENT) {
            commentService.deleteCommentByAdmin(contentId);
        } else if (contentType == ReportType.REVIEW) { // 👇 REVIEW 타입 처리 로직 추가
            mapService.deactivateReviewByAdmin(contentId);
        }
    }
    @Transactional
    public void deactivateCommentByAdmin(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
        if (comment.isActive()) {
            comment.deactivate();
        }
    }

    // 관리자용 게시글 검색
    @Transactional(readOnly = true)
    public Page<AdminBoardDto> searchBoardsByAdmin(String type, String keyword, Pageable pageable) {
        Page<Board> boards = boardRepository.searchAllBy(type, keyword, pageable);
        return boards.map(AdminBoardDto::new);
    }

    // 관리자용 댓글 검색
    @Transactional(readOnly = true)
    public Page<AdminCommentDto> searchCommentsByAdmin(String keyword, Pageable pageable) {
        Page<Comment> comments = commentRepository.searchAllBy(keyword, pageable);
        return comments.map(AdminCommentDto::new);
    }

    // --- Helper Methods ---
    private void deactivateReportedContent(Long contentId, ReportType contentType) {
        // [수정] 변경된 메서드 이름으로 호출
        if (contentType == ReportType.BOARD) {
            boardService.deactivateBoardByAdmin(contentId);
        } else if (contentType == ReportType.COMMENT) {
            commentService.deactivateCommentByAdmin(contentId);
        }
    }

    // 특정 사용자가 작성한 모든 게시글 목록 조회
    @Transactional(readOnly = true)
    public Page<AdminBoardDto> getBoardsByUser(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Page<Board> boards = boardRepository.findByAuthor(user, pageable);
        return boards.map(AdminBoardDto::new); // 기존 AdminBoardDto 재사용
    }

    // 특정 사용자가 작성한 모든 댓글 목록 조회
    @Transactional(readOnly = true)
    public Page<AdminCommentDto> getCommentsByUser(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Page<Comment> comments = commentRepository.findByUser(user, pageable);
        return comments.map(AdminCommentDto::new); // 기존 AdminCommentDto 재사용
    }
    private AdminReportDto convertReportToAdminDto(Report report) {
        User reportedUser = findReportedUser(report);
        return new AdminReportDto(report, reportedUser);
    }

    private User findReportedUser(Report report) {
        if (report.getContentType() == ReportType.BOARD) {
            return boardRepository.findById(report.getContentId())
                    .orElseThrow(() -> new IllegalArgumentException("신고된 게시글을 찾을 수 없습니다."))
                    .getAuthor();
        } else if (report.getContentType() == ReportType.COMMENT) {
            return commentRepository.findById(report.getContentId())
                    .orElseThrow(() -> new IllegalArgumentException("신고된 댓글을 찾을 수 없습니다."))
                    .getUser();
        }
        // 이 외의 타입이 있다면 예외 처리
        throw new IllegalArgumentException("지원하지 않는 신고 타입입니다.");
    }

    @Transactional
    public void reactivateContent(String contentType, Long contentId) {
        if ("board".equalsIgnoreCase(contentType)) {
            Board board = boardRepository.findById(contentId)
                    .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
            board.activate();
        } else if ("comment".equalsIgnoreCase(contentType)) {
            Comment comment = commentRepository.findById(contentId)
                    .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
            comment.activate();
        } else if ("review".equalsIgnoreCase(contentType)) {
            Review review = reviewRepository.findById(contentId)
                    .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));
            review.activate();
        } else {
            throw new IllegalArgumentException("지원하지 않는 콘텐츠 타입입니다.");
        }
    }

    @Transactional
    public void suspendUser(Long userId, SuspensionPeriod period) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        LocalDateTime suspensionEnd = null;
        switch (period) {
            case THREE_DAYS:
                suspensionEnd = LocalDateTime.now().plusDays(3);
                break;
            case SEVEN_DAYS: // 👈 수정
                suspensionEnd = LocalDateTime.now().plusDays(7);
                break;
            case TEN_DAYS: // 👈 추가
                suspensionEnd = LocalDateTime.now().plusDays(10);
                break;
            case THIRTY_DAYS: // 👈 수정
                suspensionEnd = LocalDateTime.now().plusDays(30);
                break;
            case SIX_MONTHS: // 👈 추가
                suspensionEnd = LocalDateTime.now().plusMonths(6);
                break;
            case PERMANENT:
                suspensionEnd = LocalDateTime.of(9999, 12, 31, 23, 59, 59);
                break;
        }
        user.setSuspensionEndDate(suspensionEnd);
    }
    @Transactional
    public AdminMemoResponseDto addMemoToUser(Long targetUserId, String adminEmail, AdminMemoRequestDto dto) {
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("메모 대상 사용자를 찾을 수 없습니다."));

        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new IllegalArgumentException("관리자 계정을 찾을 수 없습니다."));

        if (admin.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("메모를 작성할 권한이 없습니다.");
        }

        AdminNote note = AdminNote.builder()
                .targetUser(targetUser)
                .admin(admin)
                .content(dto.getContent())
                .build();

        AdminNote savedNote = adminNoteRepository.save(note);
        return new AdminMemoResponseDto(savedNote);
    }

    @Transactional(readOnly = true)
    public List<AdminMemoResponseDto> getMemosForUser(Long targetUserId) {
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("메모 조회 대상 사용자를 찾을 수 없습니다."));

        List<AdminNote> notes = adminNoteRepository.findByTargetUserOrderByCreatedAtDesc(targetUser);

        return notes.stream()
                .map(AdminMemoResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void bulkDeactivateBoards(List<Long> boardIds) {
        if (boardIds == null || boardIds.isEmpty()) return;

        for (Long boardId : boardIds) {
            // 리팩토링된 BoardService의 메서드를 호출해 연관 엔티티까지 모두 처리
            boardService.deactivateBoardAndAssociations(boardId);
        }
    }

    @Transactional
    public void bulkDeactivateComments(List<Long> commentIds) {
        if (commentIds == null || commentIds.isEmpty()) return;

        // Repository에 추가한 벌크 연산 쿼리를 직접 호출
        commentRepository.bulkDeactivateByIds(commentIds);
    }

    @Transactional
    public void deactivateReviewByAdmin(Long reviewId) {
        // 실제 로직은 MapService에 위임
        mapService.deactivateReviewByAdmin(reviewId);
    }

}
