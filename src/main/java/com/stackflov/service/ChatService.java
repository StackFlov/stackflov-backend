package com.stackflov.service;

import com.stackflov.domain.ChatMessage;
import com.stackflov.domain.ChatRoom;
import com.stackflov.domain.User;
import com.stackflov.dto.ChatMessageDto;
import com.stackflov.dto.ChatMessageResponseDto;
import com.stackflov.dto.ChatRoomRequestDto;
import com.stackflov.repository.ChatMessageRepository;
import com.stackflov.repository.ChatRoomRepository;
import com.stackflov.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final ChatMessageRepository chatMessageRepository;

    @Transactional
    public Long createChatRoom(String userEmail, ChatRoomRequestDto requestDto) {
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        User targetUser = userRepository.findById(requestDto.getTargetUserId())
                .orElseThrow(() -> new IllegalArgumentException("상대방 사용자를 찾을 수 없습니다."));

        // 참고: 이미 두 사용자 간의 채팅방이 있는지 확인하는 로직을 추가하면 더 좋습니다.

        Set<User> participants = new HashSet<>();
        participants.add(currentUser);
        participants.add(targetUser);

        ChatRoom newRoom = ChatRoom.builder()
                .participants(participants)
                .build();

        ChatRoom savedRoom = chatRoomRepository.save(newRoom);
        return savedRoom.getId();
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponseDto> getMessages(Long roomId) {
        // TODO: 현재 사용자가 이 채팅방에 참여하고 있는지 확인하는 권한 검사 로직 추가 필요

        List<ChatMessage> messages = chatMessageRepository.findByChatRoomId(roomId);
        return messages.stream()
                .map(ChatMessageResponseDto::new)
                .collect(Collectors.toList());
    }
    @Transactional
    public void saveMessage(ChatMessageDto messageDto) {
        User sender = userRepository.findByEmail(messageDto.getSender()) // sender를 email로 가정
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        ChatRoom chatRoom = chatRoomRepository.findById(messageDto.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));

        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(sender)
                .content(messageDto.getMessage())
                .build();

        chatMessageRepository.save(chatMessage);
    }

}