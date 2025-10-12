package com.stackflov.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class MapBoundaryDto {
    @Schema(description = "남서쪽 위도", example = "37.48")
    private Double swLat; // 남서쪽 위도
    @Schema(description = "남서쪽 경도", example = "126.95")
    private Double swLng; // 남서쪽 경도
    @Schema(description = "북동쪽 위도", example = "37.60")
    private Double neLat;
    @Schema(description = "북동쪽 경도", example = "127.12")
    private Double neLng;
}