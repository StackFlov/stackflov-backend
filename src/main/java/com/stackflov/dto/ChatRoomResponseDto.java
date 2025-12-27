package com.stackflov.dto;

import com.stackflov.domain.ChatRoom;
import com.stackflov.domain.User;
import lombok.Getter;

@Getter
public class ChatRoomResponseDto {
    private final Long roomId;
    private String otherUserNickname;

    public ChatRoomResponseDto(ChatRoom room, Long myId) {
        this.roomId = room.getId();

        // 참여자 목록에서 '나'가 아닌 첫 번째 사람을 찾아 닉네임을 가져옵니다.
        this.otherUserNickname = room.getParticipants().stream()
                .filter(user -> !user.getId().equals(myId))
                .findFirst()
                .map(User::getNickname)
                .orElse("알 수 없는 사용자");
    }
}