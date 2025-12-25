package com.stackflov.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stackflov.config.CustomUserPrincipal;
import com.stackflov.domain.ChatRoom;
import com.stackflov.domain.User;
import com.stackflov.jwt.JwtProvider;
import com.stackflov.repository.ChatRoomRepository;
import com.stackflov.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ObjectMapper objectMapper;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        // wrap 대신 getAccessor를 사용하여 기존 세션 정보를 유지합니다.
        StompHeaderAccessor acc = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (acc == null) return message;

        StompCommand cmd = acc.getCommand();
        if (cmd == null) return message;

        switch (cmd) {
            case CONNECT -> handleConnect(acc);
            case SUBSCRIBE -> handleSubscribe(acc);
            case SEND -> handleSend(message, acc);
            default -> { /* noop */ }
        }
        return message;
    }

    private void handleConnect(StompHeaderAccessor acc) {
        // CONNECT 시 JWT 검증 및 Principal 주입
        String auth = acc.getFirstNativeHeader("Authorization");
        String token = resolveBearer(auth);
        if (!StringUtils.hasText(token) || !jwtProvider.validateToken(token)) {
            throw new org.springframework.security.access.AccessDeniedException("유효하지 않은 토큰입니다.");
        }
        String email = jwtProvider.getEmail(token); // 프로젝트에 맞는 클레임 추출 메서드 사용
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("사용자를 찾을 수 없습니다."));

        var principal = CustomUserPrincipal.of(user.getId(), user.getEmail(), user.getRole());
        var authToken = new UsernamePasswordAuthenticationToken(
                principal, null, List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
        acc.setUser(authToken);
    }

    private void handleSubscribe(StompHeaderAccessor acc) {
        // /sub/chat/room/{roomId} 구독 요청 시 참가자 검증
        String dest = acc.getDestination();
        Long roomId = extractRoomIdFromDestination(dest, "/sub/chat/room/");
        if (roomId != null) {
            ensureParticipant(acc, roomId);
        }
    }

    private void handleSend(Message<?> message, StompHeaderAccessor acc) {
        // /pub/chat/message 로 SEND 시 payload에서 roomId 읽어 참가자 검증
        String dest = acc.getDestination();
        if ("/pub/chat/message".equals(dest)) {
            Long roomId = extractRoomIdFromPayload(message);
            if (roomId != null) {
                ensureParticipant(acc, roomId);
            }
        }
    }

    private void ensureParticipant(StompHeaderAccessor acc, Long roomId) {
        var auth = acc.getUser();
        // 여기서 null이 반환되어 "인증이 필요합니다" 에러가 났던 것입니다.
        if (auth == null || !(auth instanceof UsernamePasswordAuthenticationToken token)) {
            throw new org.springframework.security.access.AccessDeniedException("인증이 필요합니다.");
        }
        String email;
        if (token.getPrincipal() instanceof CustomUserPrincipal p) email = p.getEmail();
        else email = token.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("사용자를 찾을 수 없습니다."));
        ChatRoom room = chatRoomRepository.findByIdWithParticipants(roomId)
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("채팅방을 찾을 수 없습니다."));

        // 이제 room.getParticipants()를 호출해도 에러가 나지 않습니다.
        boolean ok = room.getParticipants().stream().anyMatch(u -> u.getId().equals(user.getId()));

        if (!ok) {
            throw new org.springframework.security.access.AccessDeniedException("채팅방 참가자만 접근할 수 있습니다.");
        }
    }

    private String resolveBearer(String authorization) {
        if (!StringUtils.hasText(authorization)) return null;
        if (authorization.startsWith("Bearer ")) return authorization.substring(7);
        return authorization;
    }

    private Long extractRoomIdFromDestination(String destination, String prefix) {
        if (!StringUtils.hasText(destination) || !destination.startsWith(prefix)) return null;
        try {
            String tail = destination.substring(prefix.length());
            int slash = tail.indexOf('/');
            String idStr = (slash >= 0) ? tail.substring(0, slash) : tail;
            return Long.valueOf(idStr);
        } catch (Exception e) {
            return null;
        }
    }

    private Long extractRoomIdFromPayload(Message<?> message) {
        try {
            Object payload = message.getPayload();
            String json;
            if (payload instanceof byte[] bytes) json = new String(bytes, StandardCharsets.UTF_8);
            else json = String.valueOf(payload);
            JsonNode node = objectMapper.readTree(json);
            if (node.hasNonNull("roomId")) return node.get("roomId").asLong();
        } catch (Exception ignored) {}
        return null;
    }
}
