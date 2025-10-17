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
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final ChatMessageRepository chatMessageRepository;

    @Transactional
    public Long createChatRoom(String userEmail, ChatRoomRequestDto requestDto) {
        User me = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        if (me.getId().equals(requestDto.getTargetUserId())) {
            throw new IllegalArgumentException("본인과는 채팅방을 생성할 수 없습니다.");
        }
        User you = userRepository.findById(requestDto.getTargetUserId())
                .orElseThrow(() -> new IllegalArgumentException("상대방 사용자를 찾을 수 없습니다."));

        return chatRoomRepository.findDirectRoomBetween(me.getId(), you.getId())
                .map(ChatRoom::getId)
                .orElseGet(() -> {
                    ChatRoom newRoom = ChatRoom.builder()
                            .participants(new java.util.HashSet<>(java.util.List.of(me, you)))
                            .build();
                    return chatRoomRepository.save(newRoom).getId();
                });
    }

    private boolean isParticipant(ChatRoom room, User user) {
        Long uid = user.getId();
        return room.getParticipants().stream().anyMatch(u -> u.getId().equals(uid));
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponseDto> getMessages(Long roomId, String requesterEmail) {
        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));

        if (!isParticipant(room, requester)) {
            throw new org.springframework.security.access.AccessDeniedException("채팅방 참가자만 열람할 수 있습니다.");
        }

        List<ChatMessage> messages = chatMessageRepository.findByChatRoomIdOrderBySentAtAsc(roomId);
        return messages.stream().map(ChatMessageResponseDto::new).toList();
    }

    @Transactional
    public ChatMessageResponseDto saveMessage(ChatMessageDto dto, String senderEmail) {
        if (senderEmail == null) throw new org.springframework.security.access.AccessDeniedException("인증되지 않은 사용자입니다.");

        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        ChatRoom room = chatRoomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));

        if (!isParticipant(room, sender)) {
            throw new org.springframework.security.access.AccessDeniedException("채팅방 참가자만 메시지를 보낼 수 있습니다.");
        }

        ChatMessage saved = chatMessageRepository.save(
                ChatMessage.builder().chatRoom(room).sender(sender).content(dto.getMessage()).build()
        );
        return new ChatMessageResponseDto(saved);
    }
}