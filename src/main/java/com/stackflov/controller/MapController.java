package com.stackflov.controller;

import com.stackflov.config.CustomUserPrincipal;
import com.stackflov.dto.*;
import com.stackflov.service.MapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
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
        - 인증: `Authorization: Bearer <accessToken>`
        - 요청 Content-Type: `multipart/form-data`  
          - 한 요청 안에 **JSON 파트(`data`)**와 **파일 파트(`images`)**를 함께 전송합니다.
          """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "생성된 리뷰 ID(number) 반환"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping(value = "reviews", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Long> createReview(
            @RequestPart("data") ReviewRequestDto dto, // JSON 데이터
            @RequestPart(value = "images", required = false) List<MultipartFile> images, // 이미지 파일 목록
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        Long reviewId = mapService.createReview(dto, principal.getEmail(), images);
        return ResponseEntity.ok(reviewId);
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
    @PutMapping(value = "/reviews/{reviewId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateReview(
            @PathVariable("reviewId") Long reviewId,
            @RequestPart("data") ReviewRequestDto dto,              // ← key 이름 일치
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
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
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        mapService.deleteReview(reviewId, principal.getEmail());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/reviews")
    public ResponseEntity<Page<ReviewListResponseDto>> getReviews(
            @ParameterObject Pageable pageable,
            @AuthenticationPrincipal @Nullable CustomUserPrincipal principal
    ) {
        String email = principal == null ? null : principal.getEmail();
        Page<ReviewListResponseDto> reviews = mapService.getReviews(pageable, email);
        return ResponseEntity.ok(reviews);
    }
    @Operation(summary = "리뷰 상세 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "404", description = "리뷰 없음")
    })
    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewDetailResponseDto> getReviewDetail(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal @Nullable CustomUserPrincipal principal
    ) {
        String email = (principal == null) ? null : principal.getEmail();
        return ResponseEntity.ok(mapService.getReviewDetail(reviewId, email));
    }
}