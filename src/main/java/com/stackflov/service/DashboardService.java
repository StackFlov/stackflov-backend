package com.stackflov.service;

import com.stackflov.domain.ReportStatus;
import com.stackflov.dto.DailyStatDto;
import com.stackflov.dto.DashboardStatsDto;
import com.stackflov.dto.DetailedStatsDto;
import com.stackflov.repository.*;
import com.stackflov.repository.projection.DailyStatProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;
    private final ReportRepository reportRepository;
    private final ReviewRepository reviewRepository;

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

    public DetailedStatsDto getDetailedStats() {
        LocalDateTime startDate = LocalDate.now().minusDays(6).atStartOfDay(); // 오늘 포함 최근 7일

        // 1. 각 Repository에서 통계 데이터 조회
        List<DailyStatProjection> signupData = userRepository.countDailySignups(startDate);
        List<DailyStatProjection> boardData = boardRepository.countDailyBoards(startDate);
        List<DailyStatProjection> commentData = commentRepository.countDailyComments(startDate);
        List<DailyStatProjection> reviewData = reviewRepository.countDailyReviews(startDate);

        // 2. 데이터가 없는 날짜는 0으로 채워주는 처리
        List<DailyStatDto> dailySignups = processDailyStats(signupData, startDate.toLocalDate());
        List<DailyStatDto> dailyBoards = processDailyStats(boardData, startDate.toLocalDate());
        List<DailyStatDto> dailyComments = processDailyStats(commentData, startDate.toLocalDate());
        List<DailyStatDto> dailyReviews = processDailyStats(reviewData, startDate.toLocalDate());

        // 3. 최종 DTO 빌드 후 반환
        return DetailedStatsDto.builder()
                .dailySignups(dailySignups)
                .dailyBoards(dailyBoards)
                .dailyComments(dailyComments)
                .dailyReviews(dailyReviews)
                .build();
    }
    private List<DailyStatDto> processDailyStats(List<DailyStatProjection> data, LocalDate startDate) {
        Map<LocalDate, Long> dataMap = data.stream()
                .collect(Collectors.toMap(DailyStatProjection::getDate, DailyStatProjection::getCount));

        List<DailyStatDto> result = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate currentDate = startDate.plusDays(i);
            long count = dataMap.getOrDefault(currentDate, 0L);
            result.add(new DailyStatDto(currentDate, count));
        }
        return result;
    }
}