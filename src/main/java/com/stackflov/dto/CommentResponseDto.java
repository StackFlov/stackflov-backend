package com.stackflov.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentResponseDto {
    private Long id;
    private String content;
    private Long authorId;
    private String authorNickname;
    private String authorEmail;

    private Long boardId;
    private Long reviewId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
