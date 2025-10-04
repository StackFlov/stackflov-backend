package com.stackflov.controller;

import com.stackflov.config.CustomUserPrincipal;
import com.stackflov.dto.*;
import com.stackflov.service.MapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Map", description = "지도 위치/리뷰 생성·조회·수정·삭제 API")
@RestController
@RequestMapping("/map")
@RequiredArgsConstructor
public class MapController {

    private final MapService mapService;

    // 새로운 위치(화살표) 생성 API
    @Operation(
            summary = "리뷰 생성 (멀티파트)",
            description = "`dto` 파트에 JSON, `images` 파트에 파일 목록을 담아 특정 위치(locationId)에 리뷰를 생성합니다."
    )
    @PostMapping(value = "/locations/{locationId}/reviews", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Long> createReview(
            @PathVariable Long locationId,
            @RequestPart("dto") ReviewRequestDto dto, // JSON 데이터
            @RequestPart(value = "images", required = false) List<MultipartFile> images, // 이미지 파일 목록
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        Long reviewId = mapService.createReview(locationId, dto, principal.getEmail(), images);
        return ResponseEntity.ok(reviewId);
    }
    // 특정 위치의 모든 리뷰 조회 API
    @Operation(
            summary = "특정 위치의 리뷰 목록 조회",
            description = "locationId에 해당하는 모든 리뷰를 조회합니다."
    )
    @GetMapping("/locations/{locationId}/reviews")
    public ResponseEntity<List<ReviewResponseDto>> getReviews(@PathVariable Long locationId) {
        List<ReviewResponseDto> reviews = mapService.getReviews(locationId);
        return ResponseEntity.ok(reviews);
    }

    // (지도 영역 내 위치 조회 API가 여기에 추가됩니다)
    @Operation(
            summary = "지도 영역 내 위치 조회",
            description = "지도 경계(`MapBoundaryDto`)로 필터링하여 영역 내 위치 목록을 조회합니다."
    )
    @GetMapping("/locations")
    public ResponseEntity<List<LocationDto>> getLocationsInMap(@ModelAttribute MapBoundaryDto dto) {
        List<LocationDto> locations = mapService.getLocationsInMap(dto);
        return ResponseEntity.ok(locations);
    }

    @Operation(
            summary = "리뷰 수정",
            description = "특정 리뷰(reviewId)의 내용을 수정합니다."
    )
    @PutMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> updateReview(
            @PathVariable Long reviewId,
            @RequestBody ReviewRequestDto dto,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        mapService.updateReview(reviewId, dto, principal.getEmail());
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "리뷰 삭제",
            description = "특정 리뷰(reviewId)를 삭제합니다."
    )
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        mapService.deleteReview(reviewId, principal.getEmail());
        return ResponseEntity.noContent().build();
    }
}