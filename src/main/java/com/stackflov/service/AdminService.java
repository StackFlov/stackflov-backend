package com.stackflov.service;

import com.stackflov.domain.*;
import com.stackflov.dto.*;
import com.stackflov.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
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

        return users.map(u -> {
            long boards = boardRepository.countByAuthor_Id(u.getId());
            long comments = commentRepository.countByUser_Id(u.getId());
            return new AdminUserDto(u, boards, comments);
        });
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
        try {
            if (contentType == ReportType.BOARD) {
                boardService.deactivateBoardByAdmin(contentId);
            } else if (contentType == ReportType.COMMENT) {
                commentService.deleteCommentByAdmin(contentId);
            } else if (contentType == ReportType.REVIEW) {
                // 🚩 여기서 터지는 예외를 무시하거나 안전하게 처리
                mapService.deactivateReviewByAdmin(contentId);
            }
        } catch (IllegalArgumentException e) {
            // 이미 삭제된 경우 로그만 남기고 정상 진행 (신고 상태는 처리 완료로 바꿔야 하니까)
            log.warn("이미 삭제된 콘텐츠입니다. (타입: {}, ID: {})", contentType, contentId);
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
        // 공통
        User reportedUser = findReportedUser(report); // 아래 switch에서 다시 덮어쓸 수도 있음
        Long parentBoardId = null;
        Long parentReviewId = null;
        String parentType = null;
        String contentUrl = null;
        String excerpt = null;

        switch (report.getContentType()) {
            case BOARD -> {
                Board b = boardRepository.findById(report.getContentId()).orElse(null);
                reportedUser = b.getAuthor();
                parentType = "BOARD";
                parentBoardId = b.getId(); // 의미상 부모=자기 자신
                contentUrl = "/trace/detail/" + b.getId();
                excerpt = cut(b.getContent(), 120);
            }
            case COMMENT -> {
                Comment c = commentRepository.findById(report.getContentId()).orElse(null);
                reportedUser = c.getUser();

                // (선택) 부모 라우팅/딥링크까지 만들고 싶다면:
                if (c.getBoard() != null) {
                    parentType = "BOARD";
                    parentBoardId = c.getBoard().getId();
                    contentUrl = "/trace/detail" + parentBoardId + "#comment-" + c.getId();
                } else if (c.getReview() != null) {
                    parentType = "REVIEW";
                    parentReviewId = c.getReview().getId();
                    contentUrl = "/reviews/" + parentReviewId + "#comment-" + c.getId();
                } else {
                    excerpt = "(삭제된 댓글입니다)";
                    contentUrl = "#";
                }

                excerpt = cut(c.getContent(), 120);
            }
            case REVIEW -> {
                Review r = reviewRepository.findById(report.getContentId()).orElse(null);
                if (r != null) {
                    reportedUser = r.getAuthor();
                    parentType = "REVIEW";
                    parentReviewId = r.getId();
                    contentUrl = "/reviews/" + r.getId();
                    excerpt = cut(r.getContent(), 120);
                } else {
                    excerpt = "(삭제된 리뷰입니다)";
                    contentUrl = "#";
                }
            }
            default -> {
                excerpt = "(알 수 없는 타입: " + report.getContentType() + ")";
                contentUrl = "#";
            }
        }

        return new AdminReportDto(
                report, reportedUser,
                parentBoardId, parentReviewId, parentType,
                contentUrl, excerpt
        );
    }

    // 미리보기 120자 정도 잘라주는 유틸
    private static String cut(String s, int len) {
        if (s == null) return null;
        s = s.strip();
        return s.length() <= len ? s : s.substring(0, len) + "…";
    }

    private User findReportedUser(Report report) {
        try {
            return switch (report.getContentType()) {
                case BOARD -> boardRepository.findById(report.getContentId()).map(Board::getAuthor).orElse(null);
                case COMMENT -> commentRepository.findById(report.getContentId()).map(Comment::getUser).orElse(null);
                case REVIEW -> reviewRepository.findById(report.getContentId()).map(Review::getAuthor).orElse(null);
                default -> null;
            };
        } catch (Exception e) {
            return null;
        }
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
        commentRepository.bulkDeactivateByReviewId(reviewId);
    }

    @Transactional
    public AdminMemoResponseDto deleteMemoOfUser(Long targetUserId, Long memoId, String adminEmail) {
        // 관리자 계정 확인
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new IllegalArgumentException("관리자 계정을 찾을 수 없습니다."));
        if (admin.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("메모를 삭제할 권한이 없습니다.");
        }

        // 메모 조회
        AdminNote note = adminNoteRepository.findById(memoId)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 메모를 찾을 수 없습니다."));

        // 대상 사용자 일치 검증
        if (!note.getTargetUser().getId().equals(targetUserId)) {
            throw new IllegalArgumentException("요청한 userId와 메모의 대상 사용자가 일치하지 않습니다.");
        }

        // 실제 삭제(하드 삭제)
        adminNoteRepository.delete(note);

        // 삭제 전 정보 반환(프론트에서 토스트/로그용으로 활용)
        return new AdminMemoResponseDto(note);
    }

    @Transactional
    public AdminMemoResponseDto updateMemoOfUser(Long targetUserId, Long memoId, String adminEmail, AdminMemoRequestDto dto) {
        // 관리자 확인
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new IllegalArgumentException("관리자 계정을 찾을 수 없습니다."));
        if (admin.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("메모를 수정할 권한이 없습니다.");
        }

        // 메모 조회
        AdminNote note = adminNoteRepository.findById(memoId)
                .orElseThrow(() -> new IllegalArgumentException("수정할 메모를 찾을 수 없습니다."));

        // 대상 사용자 일치 검증
        if (!note.getTargetUser().getId().equals(targetUserId)) {
            throw new IllegalArgumentException("요청 userId와 메모의 대상 사용자가 일치하지 않습니다.");
        }

        // (선택) 본인 작성만 수정 허용하려면:
        // if (!note.getAdmin().getId().equals(admin.getId())) {
        //     throw new IllegalArgumentException("본인이 작성한 메모만 수정할 수 있습니다.");
        // }

        // 내용 수정
        note.setContent(dto.getContent());
        AdminNote saved = adminNoteRepository.save(note);

        return new AdminMemoResponseDto(saved);
    }

    @Transactional
    public void updateUserLevelExp(Long userId, LevelExpRequestDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

        int oldLevel = user.getLevel(); // 이전 레벨 저장 (비교용)
        user.setLevel(dto.getLevel());
        user.setExp(dto.getExp());

        // ✅ 관리자가 레벨을 수정했을 때 알림 발송 로직 추가
        // 레벨이 상승하거나 변경되었을 때만 알림을 보내는 것이 좋습니다.
        String levelName = getLevelName(dto.getLevel());
        notificationService.notify(
                user,
                NotificationType.LEVELUP, // 유저님이 정하신 LEVELUP 타입 사용
                "관리자에 의해 등급이 [" + levelName + "]로 조정되었습니다.",
                "/mypage" // 클릭 시 이동할 경로
        );
    }

    private String getLevelName(int level) {
        String[] names = {"입문자", "먼지 먹는 하마", "편의점 미슐랭", "배달 앱 VVIP",
                "우리 동네 반장님", "빨래 건조대 수호자", "프로 자취 연금술사",
                "당근 온도 99도", "지박령", "자취방 만렙 교수", "StackFlov 성주"};
        if (level < 0) return names[0];
        if (level >= names.length) return names[names.length - 1];
        return names[level];
    }

}
