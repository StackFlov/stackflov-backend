package com.stackflov.dto;

import com.stackflov.domain.Bookmark;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class BookmarkResponseDto {
    private final Long bookmarkId;
    private final Long userId;
    private final String userEmail;
    private final Long boardId;
    private final String boardTitle;
    private final LocalDateTime createdAt;

    public BookmarkResponseDto(Bookmark bookmark) {
        this.bookmarkId = bookmark.getId();
        this.userId = bookmark.getUser().getId();
        this.userEmail = bookmark.getUser().getEmail(); // 필요한 경우 User 엔티티에 getEmail() 추가
        this.boardId = bookmark.getBoard().getId();
        this.boardTitle = bookmark.getBoard().getTitle(); // 필요한 경우 Board 엔티티에 getTitle() 추가
        this.createdAt = bookmark.getCreatedAt();
    }
}