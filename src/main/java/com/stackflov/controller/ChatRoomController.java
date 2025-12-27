package com.stackflov.controller;

import com.stackflov.config.CustomUserPrincipal;
import com.stackflov.dto.ChatMessageResponseDto;
import com.stackflov.dto.ChatRoomRequestDto;
import com.stackflov.dto.ChatRoomResponseDto;
import com.stackflov.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Chat Rooms", description = "채팅방 생성 및 메시지 조회 API")
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatService chatService;

    @Operation(
            summary = "채팅방 생성",
            description = "대상 사용자와 1:1 채팅방을 생성합니다."
    )
    @PostMapping("/rooms")
    public ResponseEntity<Long> createRoom(@AuthenticationPrincipal CustomUserPrincipal principal,
                                           @RequestBody ChatRoomRequestDto requestDto) {
        Long roomId = chatService.createChatRoom(principal.getEmail(), requestDto);
        return ResponseEntity.ok(roomId);
    }

    @Operation(
            summary = "채팅방 메시지 조회",
            description = "roomId에 해당하는 채팅방의 메시지 목록을 조회합니다."
    )
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<List<ChatMessageResponseDto>> getMessages(
            @PathVariable Long roomId,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        List<ChatMessageResponseDto> messages = chatService.getMessages(roomId, principal.getEmail());
        return ResponseEntity.ok(messages);
    }

    @Operation(summary = "내 채팅방 목록 조회", description = "현재 로그인한 유저가 참여 중인 채팅방 목록을 반환합니다.")
    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoomResponseDto>> getMyRooms(@AuthenticationPrincipal CustomUserPrincipal principal) {
        List<ChatRoomResponseDto> rooms = chatService.getMyRooms(principal.getEmail());
        return ResponseEntity.ok(rooms);
    }
}
