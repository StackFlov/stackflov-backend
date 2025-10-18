package com.stackflov.controller;

import com.stackflov.config.CustomUserPrincipal;
import com.stackflov.dto.LikeRequestDto;
import com.stackflov.service.LikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Likes", description = "게시글 좋아요 추가/취소 API")
@RestController
@RequestMapping("/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @Operation(
            summary = "좋아요 추가 (게시글/리뷰)",
            description = """
            **HTTP**
            - POST /likes
            - 인증: Authorization: Bearer <accessToken>
            - 대상: boardId 또는 reviewId 중 **정확히 하나만** 전달
            
            **요청 본문 (application/json → LikeRequestDto)**
            - 게시글 좋아요 예시:
              z
            - 리뷰 좋아요 예시:
              {
                "reviewId": 987
              }
            
            **검증 규칙**
            - boardId, reviewId 중 정확히 하나만 존재해야 합니다. (둘 다 있거나 둘 다 없으면 400)
            
            **응답**
            - 200 OK (Body 없음)
        """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "좋아요 추가 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청(대상 없음/중복 좋아요/검증 실패)"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "대상 게시글/리뷰 없음")
    })
    @PostMapping
    public ResponseEntity<String> addLike(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestBody LikeRequestDto dto
    ) {
        likeService.addLike(principal.getEmail(), dto);
        return ResponseEntity.ok("좋아요를 추가했습니다.");
    }

    @Operation(
            summary = "좋아요 취소 (게시글/리뷰)",
            description = """
            **HTTP**
            - DELETE /likes
            - 인증: Authorization: Bearer <accessToken>
            - 대상: boardId 또는 reviewId 중 **정확히 하나**를 쿼리스트링으로 전달
              - 예) /likes?boardId=123
              - 예) /likes?reviewId=987
            
            **요청 파라미터 (Query)**
            - boardId (Long, 선택)
            - reviewId (Long, 선택)
            - 둘 중 정확히 하나만 전달
            
            **검증 규칙**
            - boardId, reviewId 중 정확히 하나만 존재해야 합니다. (둘 다 있거나 둘 다 없으면 400)
            
            **응답**
            - 200 OK (Body 없음)
        """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "좋아요 취소 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청(기록 없음/검증 실패)"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "대상 게시글/리뷰 없음")
    })
    @DeleteMapping
    public ResponseEntity<Void> removeLike(@AuthenticationPrincipal CustomUserPrincipal me,
                                           @RequestParam(required = false) Long boardId,
                                           @RequestParam(required = false) Long reviewId) {
        LikeRequestDto dto = new LikeRequestDto();
        dto.setBoardId(boardId);
        dto.setReviewId(reviewId);
        dto.validate();
        likeService.removeLike(me.getEmail(), dto);
        return ResponseEntity.ok().build();
    }
}
