package com.stackflov.service;

import com.stackflov.domain.*;
import com.stackflov.dto.*;
import com.stackflov.repository.BoardRepository;
import com.stackflov.repository.CommentRepository;
import com.stackflov.repository.ReportRepository;
import com.stackflov.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final ReportRepository reportRepository;
    private final BoardService boardService;
    private final CommentService commentService;
    private final BoardRepository boardRepository; // 추가
    private final CommentRepository commentRepository;

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

        // User 엔티티에 역할 변경 메서드가 없으므로, 빌더를 사용해 객체를 재생성하고 저장
        User updatedUser = buildUserWithNewRole(targetUser, dto.getRole());
        userRepository.save(updatedUser);
    }

    // 사용자 계정 상태 변경 (활성/비활성)
    @Transactional
    public void updateUserStatus(Long userId, UserStatUpdateRequestDto dto, String adminEmail) { // [수정] DTO 이름을 변경된 이름으로 수정
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (targetUser.getEmail().equals(adminEmail)) {
            throw new IllegalArgumentException("자신의 계정 상태는 변경할 수 없습니다.");
        }

        if (targetUser.getRole() == Role.ADMIN && !dto.isActive() && userRepository.countByRole(Role.ADMIN) <= 1) {
            throw new IllegalStateException("마지막 남은 관리자는 비활성화할 수 없습니다.");
        }

        User updatedUser = buildUserWithNewStatus(targetUser, dto.isActive());
        userRepository.save(updatedUser);
    }

    // --- Helper Methods ---
    private User buildUserWithNewRole(User original, Role newRole) {
        return User.builder()
                .id(original.getId()).email(original.getEmail()).password(original.getPassword())
                .nickname(original.getNickname()).profileImage(original.getProfileImage())
                .socialType(original.getSocialType()).socialId(original.getSocialId())
                .level(original.getLevel()).active(original.isActive())
                .role(newRole) // 역할 변경
                .createdAt(original.getCreatedAt()).build();
    }

    private User buildUserWithNewStatus(User original, boolean newStatus) {
        return User.builder()
                .id(original.getId()).email(original.getEmail()).password(original.getPassword())
                .nickname(original.getNickname()).profileImage(original.getProfileImage())
                .socialType(original.getSocialType()).socialId(original.getSocialId())
                .level(original.getLevel()).role(original.getRole())
                .active(newStatus) // 상태 변경
                .createdAt(original.getCreatedAt()).build();
    }
    @Transactional(readOnly = true)
    public Page<AdminReportDto> getPendingReports(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Report> reports = reportRepository.findByStatus(ReportStatus.PENDING, pageable);
        return reports.map(AdminReportDto::new);
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
    }

    private void deleteReportedContent(Long contentId, ReportType contentType) {
        if (contentType == ReportType.BOARD) {
            boardService.deactivateBoardByAdmin(contentId);
        } else if (contentType == ReportType.COMMENT) {
            commentService.deleteCommentByAdmin(contentId);
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

}
