package com.stackflov.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponseDto {
    private Long id;
    private String content;
    private Long authorId;
    private String authorNickname;
    private String authorEmail;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
