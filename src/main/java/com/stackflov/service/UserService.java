package com.stackflov.service;

import com.stackflov.domain.User;
import com.stackflov.dto.LoginRequestDto;
import com.stackflov.dto.TokenResponseDto;
import com.stackflov.jwt.JwtProvider;
import com.stackflov.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RedisService redisService;

    public TokenResponseDto login(LoginRequestDto requestDto) {
        User user = userRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        String accessToken = jwtProvider.createAccessToken(user.getEmail(), user.getRole().name());
        String refreshToken = jwtProvider.createRefreshToken(user.getEmail());

        redisService.save("RT:" + user.getEmail(), refreshToken, jwtProvider.REFRESH_TOKEN_EXPIRE_TIME);

        return new TokenResponseDto(accessToken, refreshToken);
    }
}
