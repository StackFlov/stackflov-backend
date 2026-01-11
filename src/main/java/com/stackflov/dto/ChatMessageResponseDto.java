package com.stackflov.dto;

import com.stackflov.domain.ChatMessage;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ChatMessageResponseDto {

    private final Long messageId;
    private final String senderNickname;
    private final String content;
    private String senderEmail;
    private final LocalDateTime sentAt;
    private final boolean isRead;

    public ChatMessageResponseDto(ChatMessage message) {
        this.messageId = message.getId();
        this.senderNickname = message.getSender().getNickname();
        this.content = message.getContent();
        this.senderEmail = message.getSender().getEmail();
        this.sentAt = message.getSentAt();
        this.isRead = message.isRead();
    }
}