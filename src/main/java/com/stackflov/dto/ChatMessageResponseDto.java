package com.stackflov.dto;

import com.stackflov.domain.ChatMessage;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ChatMessageResponseDto {

    private final Long messageId;
    private final String senderNickname;
    private final String content;
    private final LocalDateTime sentAt;

    public ChatMessageResponseDto(ChatMessage message) {
        this.messageId = message.getId();
        this.senderNickname = message.getSender().getNickname();
        this.content = message.getContent();
        this.sentAt = message.getSentAt();
    }
}