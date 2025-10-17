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
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

@Tag(name = "Chat (STOMP)", description = "STOMP over WebSocket: publish & subscribe 경로 안내")
@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessageSendingOperations messagingTemplate;
    private final ChatService chatService; // 👈 의존성 주입 추가

    @Operation(
            summary = "채팅 메시지 발행 (STOMP)",
            description = """
            실제 전송은 STOMP로 수행됩니다.
            - Publish: /pub/chat/message
            - Subscribe: /sub/chat/room/{roomId}
        """
    )
    @MessageMapping("/chat/message")
    public void message(@org.springframework.messaging.handler.annotation.Payload ChatMessageDto message,
                        Authentication authentication) {
        String email = null;
        Object principal = (authentication != null ? authentication.getPrincipal() : null);
        if (principal instanceof CustomUserPrincipal p) {
            email = p.getEmail();
        } else if (authentication != null) {
            email = authentication.getName();
        }
        ChatMessageResponseDto saved = chatService.saveMessage(message, email);
        messagingTemplate.convertAndSend("/sub/chat/room/" + message.getRoomId(), saved);
    }
}