package com.stackflov.dto;

import com.stackflov.domain.ChatRoom;
import com.stackflov.domain.User;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class ChatRoomResponseDto {
    private final Long roomId;
    private String otherUserNickname;
    private String otherUserProfileUrl;
    private String lastMessage;       // 추가: 목록에 보여줄 마지막 메시지
    private LocalDateTime lastMessageTime; // 추가: 마지막 메시지 시간
    private long unreadCount;         // 추가: 빨간색 숫자로 표시될 개수

    // 생성자를 확장합니다.
    public ChatRoomResponseDto(ChatRoom room, Long myId, String lastMessage, LocalDateTime lastMessageTime, long unreadCount, String otherUserProfileUrl) {
        this.roomId = room.getId();

        this.otherUserNickname = room.getParticipants().stream()
                .filter(user -> !user.getId().equals(myId))
                .findFirst()
                .map(User::getNickname)
                .orElse("알 수 없는 사용자");
        this.otherUserProfileUrl = otherUserProfileUrl;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
        this.unreadCount = unreadCount;
    }
}