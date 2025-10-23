package com.stackflov.dto;

import com.stackflov.domain.ReportReason;
import com.stackflov.domain.ReportType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class ReportRequestDto {
    @Schema(description = "신고 대상의 ID", example = "123")
    @NotNull
    private Long contentId;

    @Schema(description = "콘텐츠 타입", example = "BOARD")
    @NotNull
    private ReportType contentType;

    @Schema(description = "신고 사유", example = "SPAM")
    @NotNull
    private ReportReason reason;

    @Schema(description = "상세 사유(OTHER일 때 필수)", example = "광고 링크 도배")
    @Size(max = 2000)
    private String details;
}