package com.stackflov.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
public class CommentRequestDto {
    private Long boardId;  // 댓글을 달 게시글 ID
    private String title;
    private String content;  // 댓글 내용
}

