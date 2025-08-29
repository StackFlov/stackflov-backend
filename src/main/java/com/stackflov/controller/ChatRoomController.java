package com.stackflov.controller;

import com.stackflov.dto.ChatMessageResponseDto;
import com.stackflov.dto.ChatRoomRequestDto;
import com.stackflov.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatService chatService;

    @PostMapping("/rooms")
    public ResponseEntity<Long> createRoom(@RequestAttribute("email") String userEmail,
                                           @RequestBody ChatRoomRequestDto requestDto) {
        Long roomId = chatService.createChatRoom(userEmail, requestDto);
        return ResponseEntity.ok(roomId);
    }

    // (채팅방 목록 조회, 이전 메시지 조회 API가 여기에 추가됩니다)
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<List<ChatMessageResponseDto>> getMessages(@PathVariable Long roomId) {
        // TODO: @RequestAttribute("email") String userEmail 파라미터를 받아
        //       서비스 계층에서 권한 검사를 수행하도록 수정 필요
        List<ChatMessageResponseDto> messages = chatService.getMessages(roomId);
        return ResponseEntity.ok(messages);
    }
}