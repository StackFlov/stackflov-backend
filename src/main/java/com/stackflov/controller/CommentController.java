package com.stackflov.controller;

import com.stackflov.dto.CommentRequestDto;
import com.stackflov.dto.CommentResponseDto;
import com.stackflov.jwt.JwtProvider;
import com.stackflov.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
// import org.springframework.security.core.context.SecurityContextHolder; // 사용하지 않으면 제거
// import org.springframework.security.core.Authentication; // 사용하지 않으면 제거

import java.util.List;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final JwtProvider jwtProvider;

    // 댓글 작성
    @PostMapping
    public ResponseEntity<CommentResponseDto> createComment(@RequestBody CommentRequestDto commentRequestDto, // 반환 타입 Long -> CommentResponseDto
                                                            @RequestHeader("Authorization") String accessToken) {
        String userEmail = getEmailFromToken(accessToken);
        CommentResponseDto response = commentService.createComment(commentRequestDto, userEmail);
        return ResponseEntity.ok(response);
    }

    // 댓글 수정
    @PutMapping("/{commentId}")
    public ResponseEntity<CommentResponseDto> updateComment(@PathVariable Long commentId, // 반환 타입 Void -> CommentResponseDto
                                                            @RequestBody CommentRequestDto commentRequestDto,
                                                            @RequestHeader("Authorization") String accessToken) {
        String userEmail = getEmailFromToken(accessToken);
        CommentResponseDto response = commentService.updateComment(commentId, commentRequestDto.getTitle(), commentRequestDto.getContent(), userEmail);
        return ResponseEntity.ok(response);
    }

    // 댓글 삭제 (실제 삭제 대신 비활성화)
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId,
                                              @RequestHeader("Authorization") String accessToken) {
        String userEmail = getEmailFromToken(accessToken);
        commentService.deleteComment(commentId, userEmail);
        return ResponseEntity.noContent().build();
    }

    // ✅ 특정 게시글의 활성 댓글 조회 (기존 findByBoardId 대체)
    @GetMapping("/board/{boardId}") // 기존 URL은 "/comments/board/{boardId}"
    public ResponseEntity<List<CommentResponseDto>> getCommentsByBoard(@PathVariable Long boardId) {
        List<CommentResponseDto> comments = commentService.getActiveCommentsByBoard(boardId);
        return ResponseEntity.ok(comments);
    }

    // JWT 토큰에서 이메일 추출
    private String getEmailFromToken(String token) {
        String jwtToken = token.replace("Bearer ", "");
        return jwtProvider.getEmail(jwtToken);
    }
}