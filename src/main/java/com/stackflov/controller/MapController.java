package com.stackflov.controller;

import com.stackflov.config.CustomUserPrincipal;
import com.stackflov.dto.*;
import com.stackflov.service.MapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/map")
@RequiredArgsConstructor
public class MapController {

    private final MapService mapService;

    // 새로운 위치(화살표) 생성 API
    @PostMapping("/locations")
    public ResponseEntity<Long> createLocation(@RequestBody LocationDto dto) {
        Long locationId = mapService.createLocation(dto);
        return ResponseEntity.ok(locationId);
    }

    // 특정 위치에 리뷰 작성 API
    @PostMapping("/locations/{locationId}/reviews")
    public ResponseEntity<Long> createReview(
            @PathVariable Long locationId,
            @RequestBody ReviewRequestDto dto,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        Long reviewId = mapService.createReview(locationId, dto, principal.getEmail());
        return ResponseEntity.ok(reviewId);
    }

    // 특정 위치의 모든 리뷰 조회 API
    @GetMapping("/locations/{locationId}/reviews")
    public ResponseEntity<List<ReviewResponseDto>> getReviews(@PathVariable Long locationId) {
        List<ReviewResponseDto> reviews = mapService.getReviews(locationId);
        return ResponseEntity.ok(reviews);
    }

    // (지도 영역 내 위치 조회 API가 여기에 추가됩니다)
    @GetMapping("/locations")
    public ResponseEntity<List<LocationDto>> getLocationsInMap(@ModelAttribute MapBoundaryDto dto) {
        List<LocationDto> locations = mapService.getLocationsInMap(dto);
        return ResponseEntity.ok(locations);
    }
}