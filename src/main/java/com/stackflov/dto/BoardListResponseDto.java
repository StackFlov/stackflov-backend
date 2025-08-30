package com.stackflov.dto;

import com.stackflov.domain.Board;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BoardListResponseDto {
    private Long id;
    private String title;
    private String authorEmail;
    private int category;
    private Long authorId;          // ✅ 추가
    private String authorNickname;  // ✅ 추가
    private String thumbnailUrl; // 이미지 중 첫 번째
    private int viewCount;               // ← 있어야 하고
    private LocalDateTime createdAt;     // ← 있어야 하고
    private LocalDateTime updatedAt;
    private boolean isBookmarked;
    private long likeCount;
    private boolean isLiked;

    public BoardListResponseDto(Board board) {
        this.id = board.getId();
        this.title = board.getTitle();
        this.authorEmail = board.getAuthor().getEmail();
        this.authorNickname = board.getAuthor().getNickname();
        this.authorId = board.getAuthor().getId();
        this.category = board.getCategory();
        this.thumbnailUrl = board.getImages().isEmpty() ? null : board.getImages().get(0).getImageUrl();
        this.viewCount = board.getViewCount();
        this.createdAt = board.getCreatedAt();
        this.updatedAt = board.getUpdatedAt();
        // isBookmarked, likeCount, isLiked 등은
        // Board 엔티티에 없는 정보이므로 서비스 로직에서 별도로 채워야 합니다.
        // 피드에서는 기본값(false, 0)으로 두어도 괜찮습니다.
        this.isBookmarked = false;
        this.likeCount = 0; // 이 부분은 필요하다면 likeRepository.countByBoard(board) 등으로 채울 수 있습니다.
        this.isLiked = false;
    }
}