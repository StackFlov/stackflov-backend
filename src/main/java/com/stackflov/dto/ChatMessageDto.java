package com.stackflov.dto;

import lombok.Data;

@Data
public class ChatMessageDto {
    private Long roomId;      // 채팅방 ID
    private String message;   // 메시지 내용
}