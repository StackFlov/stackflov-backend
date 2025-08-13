package com.stackflov.dto;

import com.stackflov.domain.Comment;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class AdminCommentDto {
    private final Long commentId;
    private final String content;
    private final Long boardId;
    private final String authorNickname;
    private final boolean isActive;
    private final LocalDateTime createdAt;

    public AdminCommentDto(Comment comment) {
        this.commentId = comment.getId();
        this.content = comment.getContent();
        this.boardId = comment.getBoard().getId();
        this.authorNickname = comment.getUser().getNickname();
        this.isActive = comment.isActive();
        this.createdAt = comment.getCreatedAt();
    }
}