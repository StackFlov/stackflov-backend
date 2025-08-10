package com.stackflov.dto;

import com.stackflov.domain.ReportStatus;
import lombok.Getter;

@Getter
public class ReportProcessRequestDto {
    private ReportStatus status; // 변경할 상태 (REVIEWED, REJECTED)
    private String adminComment; // 관리자 메모 (선택 사항)
}