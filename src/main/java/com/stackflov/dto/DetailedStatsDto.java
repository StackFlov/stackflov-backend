package com.stackflov.dto;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class DetailedStatsDto {
    private List<DailyStatDto> dailySignups;
    private List<DailyStatDto> dailyBoards;
    private List<DailyStatDto> dailyComments;
    private List<DailyStatDto> dailyReviews;
}