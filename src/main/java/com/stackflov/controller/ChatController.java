package com.stackflov.controller;

import com.stackflov.config.CustomUserPrincipal;
import com.stackflov.dto.ChatMessageDto;
import com.stackflov.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessageSendingOperations messagingTemplate;
    private final ChatService chatService; // 👈 의존성 주입 추가

    @MessageMapping("/chat/message")
    public void message(ChatMessageDto message, Authentication authentication) {
        // 인증 사용자 이메일 추출
        String email = null;
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserPrincipal p) {
            email = p.getEmail();
        } else if (authentication != null) {
            email = authentication.getName(); // fallback
        }
        chatService.saveMessage(message, email);
        messagingTemplate.convertAndSend("/sub/chat/room/" + message.getRoomId(), message);
    }
}