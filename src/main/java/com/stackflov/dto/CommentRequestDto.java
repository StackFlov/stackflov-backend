package com.stackflov.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
public class CommentRequestDto {
    private Long boardId;  // 게시글 댓글일 경우
    private Long reviewId; // 리뷰 댓글일 경우
    private String content;
}

