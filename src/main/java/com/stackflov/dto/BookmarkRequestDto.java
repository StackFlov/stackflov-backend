package com.stackflov.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookmarkRequestDto {
    private Long boardId; // 북마크할 게시글 ID
    // private Long userId; // 사용자 ID는 JWT 토큰에서 가져오므로 DTO에 포함하지 않음
}