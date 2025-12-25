package com.stackflov.controller;

import com.stackflov.config.CustomUserPrincipal;
import com.stackflov.dto.ChatMessageDto;
import com.stackflov.dto.ChatMessageResponseDto;
import com.stackflov.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

@Tag(name = "Chat (STOMP)", description = "STOMP over WebSocket: publish & subscribe 경로 안내")
@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    @MessageMapping("/chat/message")
    public void message(ChatMessageDto message, Authentication authentication) {
        // 1. 세션에서 인증 정보(CustomUserPrincipal) 꺼내기
        CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();
        String email = principal.getEmail();

        // 2. 중요: 메시지를 DB에 저장하고, 저장된 정보(ID, 발송시간 등)가 담긴 DTO 받기
        ChatMessageResponseDto savedMessage = chatService.saveMessage(message, email);

        // 3. 저장된 메시지 정보를 해당 채팅방을 구독 중인 모든 유저에게 실시간 전송
        messagingTemplate.convertAndSend("/sub/chat/room/" + message.getRoomId(), savedMessage);
    }
}