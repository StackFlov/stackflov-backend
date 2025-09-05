package com.stackflov.controller;

import com.stackflov.config.CustomUserPrincipal;
import com.stackflov.dto.ChatMessageResponseDto;
import com.stackflov.dto.ChatRoomRequestDto;
import com.stackflov.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatService chatService;

    @PostMapping("/rooms")
    public ResponseEntity<Long> createRoom(@AuthenticationPrincipal CustomUserPrincipal principal,
                                           @RequestBody ChatRoomRequestDto requestDto) {
        Long roomId = chatService.createChatRoom(principal.getEmail(), requestDto);
        return ResponseEntity.ok(roomId);
    }

    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<List<ChatMessageResponseDto>> getMessages(@PathVariable Long roomId) {
        List<ChatMessageResponseDto> messages = chatService.getMessages(roomId);
        return ResponseEntity.ok(messages);
    }
}
