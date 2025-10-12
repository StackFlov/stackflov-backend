package com.stackflov.controller;

import com.stackflov.config.CustomUserPrincipal;
import com.stackflov.dto.*;
import com.stackflov.service.MapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
            summary = "리뷰 생성 (multipart/form-data)",
            description = """
- 경로 파라미터: `locationId`
- 인증: `Authorization: Bearer <accessToken>`
- 요청 Content-Type: `multipart/form-data`  
  - 한 요청 안에 **JSON 파트(`dto`)**와 **파일 파트(`images`)**를 함께 전송합니다.

## form-data 파트 구성 (필드명 고정)
| Key     | Type                             | Content-Type           | 설명                                   |
|---------|----------------------------------|------------------------|----------------------------------------|
| dto     | **Text → JSON 문자열**           | `application/json`     | 리뷰 본문 DTO(JSON)                    |
| images  | **File (여러 개 가능)**          | `image/*`              | 리뷰 이미지(선택). 여러 장이면 키 반복 |

### `dto` 파트에 넣을 JSON 예시
```json
{
  "title": "원룸 거주 후기",
  "content": "채광 좋고 방음은 보통이에요.",
  "rating": 4
}
"""
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "생성된 리뷰 ID(number) 반환"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "locationId 없음")
    })
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
            description = """
**HTTP**
- GET /map/locations/{locationId}/reviews
- 인증: 필요 없음 (공개 조회 API)  // 인증이 필요하면 'Authorization: Bearer <accessToken>'로 바꿔주세요.

**요청 형식**
- 경로 변수: locationId (Long)
- QueryString: 없음  // 페이징이 필요하다면 ?page=0&size=10 등으로 확장

**응답 형식 (application/json)**
- Body: ReviewResponseDto[] (빈 배열 가능)
"""
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "리뷰 목록 반환"),
            @ApiResponse(responseCode = "404", description = "locationId 없음")
    })
    @GetMapping("/locations/{locationId}/reviews")
    public ResponseEntity<List<ReviewResponseDto>> getReviews(@PathVariable Long locationId) {
        List<ReviewResponseDto> reviews = mapService.getReviews(locationId);
        return ResponseEntity.ok(reviews);
    }

    // (지도 영역 내 위치 조회 API가 여기에 추가됩니다)
    @Operation(
            summary = "지도 영역 내 위치 조회",
            description = """
**HTTP**
- GET /map/locations

**쿼리 파라미터 (MapBoundaryDto, QueryString)**
- swLat (double): 남서 모서리 위도 (South-West Latitude)
- swLng (double): 남서 모서리 경도 (South-West Longitude)
- neLat (double): 북동 모서리 위도 (North-East Latitude)
- neLng (double): 북동 모서리 경도 (North-East Longitude)

**제약**
- -90 ≤ 위도 ≤ 90, -180 ≤ 경도 ≤ 180
- swLat < neLat, swLng < neLng (경계 박스 유효성)
- 인증: 필요 없음 (공개 조회 API)

**응답 형식 (application/json)**
- Body: LocationDto[] (빈 배열 가능)

**예시 요청**
- /map/locations?swLat=37.48&swLng=126.90&neLat=37.60&neLng=127.05
"""
    )
    @ApiResponse(responseCode = "200", description = "영역 내 위치 목록 반환")
    @GetMapping("/locations")
    public ResponseEntity<List<LocationDto>> getLocationsInMap(@ModelAttribute MapBoundaryDto dto) {
        List<LocationDto> locations = mapService.getLocationsInMap(dto);
        return ResponseEntity.ok(locations);
    }

    @Operation(
            summary = "리뷰 수정",
            description = """
**HTTP**
- PUT /map/reviews/{reviewId}
- 인증: Authorization: Bearer <accessToken>
- 작성자 본인만 수정 가능

**경로 파라미터**
- reviewId (Long)

**요청 본문 (application/json → ReviewRequestDto)**
- 예시:
{
  "rating": 4.0,
  "content": "밤에 조금 시끄럽지만 치안은 좋아요.",
  "images": [
    "https://cdn.stackflov.com/reviews/101_1.jpg",
    "https://cdn.stackflov.com/reviews/101_2.jpg"
  ]
}

**응답**
- 200 OK (Body 없음)
"""
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 완료"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청/권한 없음"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "리뷰 없음")
    })
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
            summary = "리뷰 삭제(비활성화)",
            description = """
**HTTP**
- DELETE /map/reviews/{reviewId}
- 인증: Authorization: Bearer <accessToken>
- 작성자 본인만 삭제(비활성화) 가능

**경로 파라미터**
- reviewId (Long)

**요청 본문**
- 없음

**응답**
- 204 No Content (Body 없음)
"""
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제(비활성화) 완료"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청/권한 없음"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "리뷰 없음")
    })
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        mapService.deleteReview(reviewId, principal.getEmail());
        return ResponseEntity.noContent().build();
    }
}