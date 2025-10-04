package com.stackflov.controller;

import com.stackflov.config.CustomUserPrincipal;
import com.stackflov.dto.CommentRequestDto;
import com.stackflov.dto.CommentResponseDto;
import com.stackflov.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Comments", description = "댓글 작성/수정/삭제 및 조회 API")
@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "댓글 작성", description = "게시글 또는 리뷰에 댓글을 작성합니다.")
    @PostMapping
    public ResponseEntity<Long> createComment(@RequestBody CommentRequestDto commentRequestDto,
                                              @AuthenticationPrincipal CustomUserPrincipal principal) {
        Long commentId = commentService.createComment(commentRequestDto, principal.getEmail());
        return ResponseEntity.ok(commentId);
    }

    @Operation(summary = "댓글 수정", description = "작성한 댓글의 내용을 수정합니다.")
    @PutMapping("/{commentId}")
    public ResponseEntity<Void> updateComment(@PathVariable Long commentId,
                                              @RequestBody CommentRequestDto commentRequestDto,
                                              @AuthenticationPrincipal CustomUserPrincipal principal) {
        commentService.updateComment(commentId, commentRequestDto.getContent(), principal.getEmail());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "댓글 삭제", description = "작성한 댓글을 삭제(비활성화)합니다.")
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId,
                                              @AuthenticationPrincipal CustomUserPrincipal principal) {
        commentService.deleteComment(commentId, principal.getEmail());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "게시글 댓글 목록 조회", description = "특정 게시글(boardId)의 댓글 목록을 조회합니다.")
    @GetMapping("/board/{boardId}")
    public ResponseEntity<List<CommentResponseDto>> getCommentsByBoard(@PathVariable Long boardId) {
        List<CommentResponseDto> comments = commentService.getCommentsByBoardId(boardId);
        return ResponseEntity.ok(comments);
    }

    @Operation(summary = "리뷰 댓글 목록 조회", description = "특정 리뷰(reviewId)의 댓글 목록을 조회합니다.")
    @GetMapping("/review/{reviewId}")
    public ResponseEntity<List<CommentResponseDto>> getCommentsByReview(@PathVariable Long reviewId) {
        List<CommentResponseDto> comments = commentService.getCommentsByReviewId(reviewId);
        return ResponseEntity.ok(comments);
    }
}
