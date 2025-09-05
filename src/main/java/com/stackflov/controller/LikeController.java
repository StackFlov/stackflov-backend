package com.stackflov.controller;

import com.stackflov.config.CustomUserPrincipal;
import com.stackflov.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @PostMapping("/{boardId}")
    public ResponseEntity<String> addLike(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long boardId) {
        likeService.addLike(principal.getEmail(), boardId);
        return ResponseEntity.ok("좋아요를 추가했습니다.");
    }

    @DeleteMapping("/{boardId}")
    public ResponseEntity<String> removeLike(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long boardId) {
        likeService.removeLike(principal.getEmail(), boardId);
        return ResponseEntity.ok("좋아요를 취소했습니다.");
    }
}
