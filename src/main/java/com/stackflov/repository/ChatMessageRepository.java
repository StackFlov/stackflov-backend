package com.stackflov.repository;

import com.stackflov.domain.ChatMessage;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByChatRoomIdOrderBySentAtAsc(Long roomId);
    Optional<ChatMessage> findFirstByChatRoomIdOrderBySentAtDesc(Long roomId);

    // 특정 방에서 내가 안 읽은 메시지 개수 세기
    // (조건: 발신자가 내가 아니고, 읽음 표시가 false인 메시지)
    long countByChatRoomIdAndSenderIdNotAndIsReadFalse(Long roomId, Long myId);

    // 채팅방 입장 시 메시지 읽음 처리
    @Modifying
    @Query("UPDATE ChatMessage m SET m.isRead = true WHERE m.chatRoom.id = :roomId AND m.sender.id != :myId")
    void markAsRead(@Param("roomId") Long roomId, @Param("myId") Long myId);
}