package com.stackflov.dto;

import com.stackflov.domain.Board;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class AdminBoardDto {
    private final Long boardId;
    private final String title;
    private final String authorNickname;
    private final boolean isActive;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public AdminBoardDto(Board board) {
        this.boardId = board.getId();
        this.title = board.getTitle();
        this.authorNickname = board.getAuthor().getNickname();
        this.isActive = board.isActive();
        this.createdAt = board.getCreatedAt();
        this.updatedAt = board.getUpdatedAt();
    }
}