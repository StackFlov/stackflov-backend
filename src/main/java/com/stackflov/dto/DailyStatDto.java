package com.stackflov.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class DailyStatDto {
    private LocalDate date;
    private long count;
}