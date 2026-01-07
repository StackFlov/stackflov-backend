package com.stackflov.websocket;

import com.stackflov.config.CustomUserPrincipal;
import com.stackflov.jwt.JwtProvider;
import com.stackflov.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor acc =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (acc == null) return message;

        // 1) CONNECT에서 Authorization: Bearer xxx 읽기
        if (StompCommand.CONNECT.equals(acc.getCommand())) {
            String auth = acc.getFirstNativeHeader("Authorization"); // 대소문자 주의
            log.debug("STOMP CONNECT Authorization header present? {}", (auth != null));
            if (auth == null || !auth.startsWith("Bearer ")) {
                throw new AccessDeniedException("유효하지 않은 토큰입니다.(누락)");
            }
            String token = auth.substring("Bearer ".length());
            if (!jwtProvider.validateToken(token)) {
                throw new AccessDeniedException("유효하지 않은 토큰입니다.(검증 실패/만료)");
            }

            String email = jwtProvider.getEmail(token);

            // 2) 사용자 조회 → CustomUserPrincipal 생성
            var user = userRepository.findByEmailAndActiveTrue(email)
                    .orElseThrow(() -> new AccessDeniedException("사용자를 찾을 수 없습니다."));

            CustomUserPrincipal principal = CustomUserPrincipal.from(user);

            // 3) 인증객체를 WebSocket 세션에 심기
            var authentication = new UsernamePasswordAuthenticationToken(
                    principal, null, principal.getAuthorities());
            acc.setUser(authentication);

            log.debug("STOMP CONNECT 인증 완료: {}", email);
        }
        // (선택) 이후 프레임도 인증 강제
        else if (StompCommand.SEND.equals(acc.getCommand())
                || StompCommand.SUBSCRIBE.equals(acc.getCommand())) {
            if (acc.getUser() == null) {
                throw new AccessDeniedException("인증 필요");
            }
        }
        return message;
    }
}