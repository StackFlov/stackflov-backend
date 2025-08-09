package com.stackflov.dto;

import com.stackflov.domain.ReportReason;
import com.stackflov.domain.ReportType;
import lombok.Getter;

@Getter
public class ReportRequestDto {
    private Long contentId;
    private ReportType contentType;
    private ReportReason reason;
    private String details; // 상세 사유 (선택)
}