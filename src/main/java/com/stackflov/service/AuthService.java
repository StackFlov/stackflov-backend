package com.stackflov.service;

import com.stackflov.domain.User;
import com.stackflov.dto.TokenResponseDto;
import com.stackflov.jwt.JwtProvider;
import com.stackflov.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtProvider jwtProvider;
    private final RedisService redisService;
    private final UserRepository userRepository;

    public TokenResponseDto reissueToken(String refreshToken) {
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
        }

        String email = jwtProvider.getEmail(refreshToken);
        String savedRefreshToken = redisService.get("RT:" + email);

        if (savedRefreshToken == null || !savedRefreshToken.equals(refreshToken)) {
            throw new IllegalArgumentException("리프레시 토큰이 일치하지 않습니다.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        String newAccessToken = jwtProvider.createAccessToken(email, user.getRole().name());
        String newRefreshToken = jwtProvider.createRefreshToken(email);

        redisService.save("RT:" + email, newRefreshToken, jwtProvider.REFRESH_TOKEN_EXPIRE_TIME);

        return new TokenResponseDto(newAccessToken, newRefreshToken);
    }

    // 변경: 토큰이 아니라 email을 받아서 로그아웃
    public void logout(String email) {
        if (email == null || email.isBlank()) return;
        redisService.delete("RT:" + email);
    }
}
