package com.stackflov.controller;

import com.stackflov.dto.CommentRequestDto;
import com.stackflov.dto.CommentResponseDto;
import com.stackflov.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<Long> createComment(@RequestBody CommentRequestDto commentRequestDto,
                                              @AuthenticationPrincipal String email) {
        Long commentId = commentService.createComment(commentRequestDto, email);
        return ResponseEntity.ok(commentId);
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<Void> updateComment(@PathVariable Long commentId,
                                              @RequestBody CommentRequestDto commentRequestDto,
                                              @AuthenticationPrincipal String email) {
        commentService.updateComment(commentId, commentRequestDto.getContent(), email);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId,
                                              @AuthenticationPrincipal String email) {
        commentService.deleteComment(commentId, email);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/board/{boardId}")
    public ResponseEntity<List<CommentResponseDto>> getCommentsByBoard(@PathVariable Long boardId) {
        List<CommentResponseDto> comments = commentService.getCommentsByBoardId(boardId);
        return ResponseEntity.ok(comments);
    }
}
