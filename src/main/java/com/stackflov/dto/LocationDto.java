package com.stackflov.dto;

import com.stackflov.domain.Location;
import lombok.Getter;

@Getter
public class LocationDto {
    private final Long id;
    private final String address;
    private final Double latitude;
    private final Double longitude;

    public LocationDto(Location location) {
        this.id = location.getId();
        this.address = location.getAddress();
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
    }
}