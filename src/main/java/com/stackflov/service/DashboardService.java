package com.stackflov.service;

import com.stackflov.domain.ReportStatus;
import com.stackflov.dto.DashboardStatsDto;
import com.stackflov.repository.BoardRepository;
import com.stackflov.repository.CommentRepository;
import com.stackflov.repository.ReportRepository;
import com.stackflov.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;
    private final ReportRepository reportRepository;

    @Transactional(readOnly = true)
    public DashboardStatsDto getDashboardStats() {
        // 오늘의 시작 시각 (예: 2023-10-27 00:00:00)
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);

        long totalUsers = userRepository.count();
        long todayNewUsers = userRepository.countByCreatedAtAfter(todayStart);
        long totalBoards = boardRepository.countByActiveTrue();
        long totalComments = commentRepository.countByActiveTrue();
        long pendingReports = reportRepository.countByStatus(ReportStatus.PENDING);

        return DashboardStatsDto.builder()
                .totalUsers(totalUsers)
                .todayNewUsers(todayNewUsers)
                .totalBoards(totalBoards)
                .totalComments(totalComments)
                .pendingReports(pendingReports)
                .build();
    }
}