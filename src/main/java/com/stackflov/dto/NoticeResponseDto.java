package com.stackflov.dto;

import com.stackflov.domain.Notice;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class NoticeResponseDto {
    private final Long id;
    private final String title;
    private final String content;
    private final String authorNickname;
    private final int viewCount;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    // 상세 조회용 생성자
    public NoticeResponseDto(Notice notice) {
        this.id = notice.getId();
        this.title = notice.getTitle();
        this.content = notice.getContent();
        this.authorNickname = notice.getAuthor().getNickname();
        this.viewCount = notice.getViewCount();
        this.createdAt = notice.getCreatedAt();
        this.updatedAt = notice.getUpdatedAt();
    }

    // 목록 조회용 생성자 (content 제외)
    public NoticeResponseDto(Long id, String title, String authorNickname, int viewCount, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.content = null; // 목록에서는 내용 제외
        this.authorNickname = authorNickname;
        this.viewCount = viewCount;
        this.createdAt = createdAt;
        this.updatedAt = null;
    }
}