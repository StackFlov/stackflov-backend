package com.stackflov.controller;

import com.stackflov.dto.ChatMessageDto;
import com.stackflov.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessageSendingOperations messagingTemplate;
    private final ChatService chatService; // 👈 의존성 주입 추가

    @MessageMapping("/chat/message")
    public void message(ChatMessageDto message) {
        // 1. 받은 메시지를 DB에 저장합니다.
        chatService.saveMessage(message);

        // 2. 해당 채팅방 구독자들에게 메시지를 전송합니다.
        messagingTemplate.convertAndSend("/sub/chat/room/" + message.getRoomId(), message);
    }
}