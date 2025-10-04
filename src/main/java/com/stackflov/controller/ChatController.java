package com.stackflov.controller;

import com.stackflov.config.CustomUserPrincipal;
import com.stackflov.dto.ChatMessageDto;
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