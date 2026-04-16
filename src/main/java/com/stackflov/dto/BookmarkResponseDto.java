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
    private final Long reviewId;
    private final String boardTitle;
    private final LocalDateTime createdAt;

    public BookmarkResponseDto(Bookmark bookmark) {
        this.bookmarkId = bookmark.getId();
        this.userId = bookmark.getUser().getId();
        this.userEmail = bookmark.getUser().getEmail();
        this.createdAt = bookmark.getCreatedAt();

        if (bookmark.getBoard() != null) {
            this.boardId = bookmark.getBoard().getId();
            this.boardTitle = bookmark.getBoard().getTitle();
            this.reviewId = null;
        }
        else if (bookmark.getReview() != null) {
            this.boardId = null;
            this.boardTitle = null;
            this.reviewId = bookmark.getReview().getId();
        } else {
            this.boardId = null;
            this.boardTitle = null;
            this.reviewId = null;
        }
    }
}