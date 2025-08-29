package com.stackflov.dto;

import com.stackflov.domain.ChatRoom;
import lombok.Getter;

@Getter
public class ChatRoomResponseDto {
    private final Long roomId;

    public ChatRoomResponseDto(ChatRoom chatRoom) {
        this.roomId = chatRoom.getId();
    }
}