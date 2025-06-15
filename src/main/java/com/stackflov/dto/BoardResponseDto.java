package com.stackflov.dto;

import com.stackflov.domain.Board;
import com.stackflov.domain.BoardImage;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class BoardResponseDto {
    private final Long boardId;
    private final Long userId;
    private final String userEmail;
    private final String title;
    private final String content;
    private final Integer category;
    private final Integer viewCount;
    private final Boolean active; // 게시글 active 상태
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final List<String> imageUrls; // 이미지 URL 목록

    public BoardResponseDto(Board board) {
        this.boardId = board.getId();
        this.userId = board.getUser().getId();
        this.userEmail = board.getUser().getEmail();
        this.title = board.getTitle();
        this.content = board.getContent();
        this.category = board.getCategory();
        this.viewCount = board.getViewCount();
        this.active = board.getActive();
        this.createdAt = board.getCreatedAt();
        this.updatedAt = board.getUpdatedAt();
        // 게시글에 연결된 이미지들의 URL을 가져와 DTO에 설정 (활성화된 이미지만 가져옴)
        this.imageUrls = board.getBoardImages().stream()
                .filter(BoardImage::getActive) // ✅ 활성화된 이미지만 필터링
                .map(BoardImage::getImageUrl)
                .collect(Collectors.toList());
    }
}