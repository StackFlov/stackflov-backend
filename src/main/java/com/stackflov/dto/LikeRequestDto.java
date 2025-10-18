package com.stackflov.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class LikeRequestDto {
    private Long boardId;   // 게시글 좋아요일 경우
    private Long reviewId;   // 리뷰 좋아요일 경우

    public boolean isBoardLike() {
        return boardId != null && reviewId == null;
    }
    public boolean isReviewLike() {
        return reviewId != null && boardId == null;
    }
    public void validate() {
        if (!(isBoardLike() || isReviewLike())) {
            throw new IllegalArgumentException("boardId 또는 reviewId 중 정확히 하나만 전달하세요.");
        }
    }
}
