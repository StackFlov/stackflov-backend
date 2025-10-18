/*
package com.stackflov.jwt;

import com.stackflov.config.CustomUserPrincipal;
import com.stackflov.domain.User;
import com.stackflov.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        // STOMP CONNECT 메시지인 경우에만 JWT 토큰을 검증합니다.
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            // 헤더에서 'Authorization' 값을 가져옵니다.
            String jwtToken = accessor.getFirstNativeHeader("Authorization");

            // 토큰이 유효한지 검증합니다.
            if (jwtToken != null && jwtToken.startsWith("Bearer ") && jwtProvider.validateToken(jwtToken.substring(7))) {
                String token = jwtToken.substring(7);
                String email = jwtProvider.getEmail(token);
                User user = userRepository.findByEmailAndActiveTrue(email)
                        .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
                CustomUserPrincipal principal = CustomUserPrincipal.from(user);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                principal,null,principal.getAuthorities());

                // SecurityContext에 인증 정보를 저장합니다.
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                // 토큰이 유효하지 않으면 예외를 발생시켜 연결을 거부합니다.
                throw new IllegalArgumentException("유효하지 않은 토큰으로 웹소켓에 연결할 수 없습니다.");
            }
        }
        return message;
    }
}*/
