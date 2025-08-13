package com.stackflov.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DashboardStatsDto {
    private final long totalUsers;
    private final long todayNewUsers;
    private final long totalBoards;
    private final long totalComments;
    private final long pendingReports;
}