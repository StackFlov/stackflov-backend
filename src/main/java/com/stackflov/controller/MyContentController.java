package com.stackflov.controller;

import com.stackflov.config.CustomUserPrincipal;
import com.stackflov.dto.*;
import com.stackflov.service.MyContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "My Contents", description = "내가 작성한 게시글/리뷰/댓글 조회 API")
@RestController
@RequestMapping("/my")
@RequiredArgsConstructor
public class MyContentController {

    private final MyContentService myContentService;

    @Operation(summary = "내 게시글 목록", description = "로그인한 사용자가 작성한 게시글을 페이징 조회합니다.")
    @GetMapping("/boards")
    public ResponseEntity<Page<BoardListResponseDto>> getMyBoards(
            @AuthenticationPrincipal CustomUserPrincipal principal, Pageable pageable) {
        return ResponseEntity.ok(myContentService.getMyBoards(principal.getEmail(), pageable));
    }

    @Operation(summary = "내 리뷰 목록", description = "로그인한 사용자가 작성한 리뷰를 페이징 조회합니다.")
    @GetMapping("/reviews")
    public ResponseEntity<Page<ReviewResponseDto>> getMyReviews(
            @AuthenticationPrincipal CustomUserPrincipal principal, Pageable pageable) {
        return ResponseEntity.ok(myContentService.getMyReviews(principal.getEmail(), pageable));
    }

    @Operation(summary = "내 댓글 목록(게시글)", description = "로그인한 사용자가 게시글에 남긴 댓글을 페이징 조회합니다.")
    @GetMapping("/comments/board")
    public ResponseEntity<Page<CommentResponseDto>> getMyBoardComments(
            @AuthenticationPrincipal CustomUserPrincipal principal, Pageable pageable) {
        return ResponseEntity.ok(myContentService.getMyBoardComments(principal.getEmail(), pageable));
    }

    @Operation(summary = "내 댓글 목록(리뷰)", description = "로그인한 사용자가 리뷰에 남긴 댓글을 페이징 조회합니다.")
    @GetMapping("/comments/review")
    public ResponseEntity<Page<CommentResponseDto>> getMyReviewComments(
            @AuthenticationPrincipal CustomUserPrincipal principal, Pageable pageable) {
        return ResponseEntity.ok(myContentService.getMyReviewComments(principal.getEmail(), pageable));
    }
}