package com.stackflov.controller;

import com.stackflov.jwt.JwtProvider;
import com.stackflov.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;
    private final JwtProvider jwtProvider;

    // 게시글 좋아요 추가
    @PostMapping("/{boardId}")
    public ResponseEntity<String> addLike(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable Long boardId) {

        String email = jwtProvider.getEmail(accessToken.substring(7));
        likeService.addLike(email, boardId);
        return ResponseEntity.ok("좋아요를 추가했습니다.");
    }

    // 게시글 좋아요 삭제
    @DeleteMapping("/{boardId}")
    public ResponseEntity<String> removeLike(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable Long boardId) {

        String email = jwtProvider.getEmail(accessToken.substring(7));
        likeService.removeLike(email, boardId);
        return ResponseEntity.ok("좋아요를 취소했습니다.");
    }
}