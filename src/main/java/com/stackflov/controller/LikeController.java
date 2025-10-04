package com.stackflov.controller;

import com.stackflov.config.CustomUserPrincipal;
import com.stackflov.service.LikeService;
import io.swagger.v3.oas.annotations.Operation;
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

    @Operation(summary = "좋아요 추가", description = "지정한 게시글(boardId)에 좋아요를 추가합니다.")
    @PostMapping("/{boardId}")
    public ResponseEntity<String> addLike(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long boardId) {
        likeService.addLike(principal.getEmail(), boardId);
        return ResponseEntity.ok("좋아요를 추가했습니다.");
    }

    @Operation(summary = "좋아요 취소", description = "지정한 게시글(boardId)의 좋아요를 취소합니다.")
    @DeleteMapping("/{boardId}")
    public ResponseEntity<String> removeLike(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long boardId) {
        likeService.removeLike(principal.getEmail(), boardId);
        return ResponseEntity.ok("좋아요를 취소했습니다.");
    }
}
