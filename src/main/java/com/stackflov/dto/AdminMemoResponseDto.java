package com.stackflov.dto;

import com.stackflov.domain.AdminNote;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AdminMemoResponseDto {
    private final Long memoId;
    private final String content;
    private final String adminNickname;
    private final LocalDateTime createdAt;

    public AdminMemoResponseDto(AdminNote note) {
        this.memoId = note.getId();
        this.content = note.getContent();
        this.adminNickname = note.getAdmin().getNickname(); // 메모를 작성한 관리자의 닉네임
        this.createdAt = note.getCreatedAt();
    }
}