package com.stackflov.dto;

import lombok.Data;

@Data
public class ChatRoomRequestDto {
    private Long targetUserId; // 1:1 채팅을 시작할 상대방의 ID
}