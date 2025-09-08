package com.stackflov.dto;

import lombok.Data;

@Data
public class MapBoundaryDto {
    private Double swLat; // 남서쪽 위도
    private Double swLng; // 남서쪽 경도
    private Double neLat; // 북동쪽 위도
    private Double neLng; // 북동쪽 경도
}