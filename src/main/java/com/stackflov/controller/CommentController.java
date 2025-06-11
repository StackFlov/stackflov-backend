package com.stackflov.controller;

import com.stackflov.dto.CommentRequestDto;
import com.stackflov.dto.CommentResponseDto;
import com.stackflov.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // 댓글 작성
    @PostMapping
    public ResponseEntity<Long> createComment(@RequestBody CommentRequestDto commentRequestDto,
                                              @RequestHeader("Authorization") String accessToken) {
        String userEmail = extractEmailFromToken(accessToken);  // JWT에서 이메일 추출
        Long commentId = commentService.createComment(commentRequestDto, userEmail);
        return ResponseEntity.ok(commentId);
    }

    // 특정 게시글에 달린 댓글 조회
    @GetMapping("/board/{boardId}")
    public ResponseEntity<List<CommentResponseDto>> getComments(@PathVariable Long boardId) {
        List<CommentResponseDto> comments = commentService.getComments(boardId);
        return ResponseEntity.ok(comments);
    }

    // 댓글 수정
    @PutMapping("/{commentId}")
    public ResponseEntity<Void> updateComment(@PathVariable Long commentId,
                                              @RequestBody CommentRequestDto commentRequestDto,
                                              @RequestHeader("Authorization") String accessToken) {
        String userEmail = extractEmailFromToken(accessToken);  // JWT에서 이메일 추출
        commentService.updateComment(commentId, commentRequestDto.getContent(), userEmail);
        return ResponseEntity.ok().build();
    }

    // 댓글 삭제
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId,
                                              @RequestHeader("Authorization") String accessToken) {
        String userEmail = extractEmailFromToken(accessToken);  // JWT에서 이메일 추출
        commentService.deleteComment(commentId, userEmail);
        return ResponseEntity.noContent().build();
    }

    private String extractEmailFromToken(String token) {
        // request에서 이메일을 추출하는 방식으로 변경
        return (String) request.getAttribute("email");
    }
}
