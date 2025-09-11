package com.stackflov.controller;

import com.stackflov.config.CustomUserPrincipal;
import com.stackflov.dto.*;
import com.stackflov.service.MyContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/my")
@RequiredArgsConstructor
public class MyContentController {

    private final MyContentService myContentService;

    @GetMapping("/boards")
    public ResponseEntity<Page<BoardListResponseDto>> getMyBoards(
            @AuthenticationPrincipal CustomUserPrincipal principal, Pageable pageable) {
        return ResponseEntity.ok(myContentService.getMyBoards(principal.getEmail(), pageable));
    }

    @GetMapping("/reviews")
    public ResponseEntity<Page<ReviewResponseDto>> getMyReviews(
            @AuthenticationPrincipal CustomUserPrincipal principal, Pageable pageable) {
        return ResponseEntity.ok(myContentService.getMyReviews(principal.getEmail(), pageable));
    }

    @GetMapping("/comments/board")
    public ResponseEntity<Page<CommentResponseDto>> getMyBoardComments(
            @AuthenticationPrincipal CustomUserPrincipal principal, Pageable pageable) {
        return ResponseEntity.ok(myContentService.getMyBoardComments(principal.getEmail(), pageable));
    }

    @GetMapping("/comments/review")
    public ResponseEntity<Page<CommentResponseDto>> getMyReviewComments(
            @AuthenticationPrincipal CustomUserPrincipal principal, Pageable pageable) {
        return ResponseEntity.ok(myContentService.getMyReviewComments(principal.getEmail(), pageable));
    }
}