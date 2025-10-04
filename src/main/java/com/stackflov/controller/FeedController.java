package com.stackflov.controller;

import com.stackflov.config.CustomUserPrincipal;
import com.stackflov.dto.BoardListResponseDto;
import com.stackflov.service.FeedService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Feed", description = "개인화 피드 조회 API")
@RestController
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;

    @Operation(
            summary = "개인화 피드 조회",
            description = "로그인한 사용자의 관심/팔로우/추천 로직에 따른 피드를 페이징으로 조회합니다."
    )
    @GetMapping("/feed")
    public ResponseEntity<Page<BoardListResponseDto>> getFeed(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            Pageable pageable) {
        Page<BoardListResponseDto> feed = feedService.getFeed(principal.getEmail(), pageable);
        return ResponseEntity.ok(feed);
    }
}
