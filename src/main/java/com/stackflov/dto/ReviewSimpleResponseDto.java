package com.stackflov.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;

public record ReviewSimpleResponseDto(
        Long reviewId,
        String address,
        String content,
        int rating,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate createdAt
) {}