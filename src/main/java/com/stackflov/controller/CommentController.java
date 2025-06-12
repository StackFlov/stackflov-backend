package com.stackflov.controller;

import com.stackflov.dto.CommentRequestDto;
import com.stackflov.dto.CommentResponseDto;
import com.stackflov.jwt.JwtProvider;
import com.stackflov.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

import java.util.List;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final JwtProvider jwtProvider;  // JwtProvider 주입

    // 댓글 작성
    @PostMapping
    public ResponseEntity<Long> createComment(@RequestBody CommentRequestDto commentRequestDto,
                                              @RequestHeader("Authorization") String accessToken) {
        String userEmail = getEmailFromToken(accessToken);  // JWT에서 이메일 추출
        Long commentId = commentService.createComment(commentRequestDto, userEmail);
        return ResponseEntity.ok(commentId);
    }

    // 댓글 수정
    @PutMapping("/{commentId}")
    public ResponseEntity<Void> updateComment(@PathVariable Long commentId,
                                              @RequestBody CommentRequestDto commentRequestDto,
                                              @RequestHeader("Authorization") String accessToken) {
        String userEmail = getEmailFromToken(accessToken);  // JWT에서 이메일 추출
        commentService.updateComment(commentId, commentRequestDto.getTitle(), commentRequestDto.getContent(), userEmail);
        return ResponseEntity.ok().build();
    }
    // 댓글 삭제
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId,
                                              @RequestHeader("Authorization") String accessToken) {
        String userEmail = getEmailFromToken(accessToken);  // JWT에서 이메일 추출
        commentService.deleteComment(commentId, userEmail);
        return ResponseEntity.noContent().build();  // 성공적으로 삭제된 경우 204 No Content 반환
    }

    // JWT 토큰에서 이메일 추출 (JwtProvider의 getEmail 메서드 사용)
    private String getEmailFromToken(String token) {
        // "Bearer " 부분을 제거하고, JWT 토큰에서 이메일을 추출
        String jwtToken = token.replace("Bearer ", "");
        return jwtProvider.getEmail(jwtToken);  // JwtProvider의 getEmail 메서드 사용
    }
}

