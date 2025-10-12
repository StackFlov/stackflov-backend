package com.stackflov.dto;

import com.stackflov.domain.Location;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class LocationDto {
    @Schema(description = "위치 ID")
    private final Long id;
    @Schema(description = "주소", example = "서울시 ○○구 ○○로 123")
    private final String address;
    @Schema(description = "위도", example = "37.12345")
    private final Double latitude;
    @Schema(description = "경도", example = "127.12345")
    private final Double longitude;

    public LocationDto(Location location) {
        this.id = location.getId();
        this.address = location.getAddress();
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
    }
}