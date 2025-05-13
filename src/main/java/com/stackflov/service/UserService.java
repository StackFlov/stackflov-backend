package com.stackflov.service;

import com.stackflov.dto.LoginRequestDto;
import com.stackflov.dto.SignupRequestDto;
import com.stackflov.dto.TokenResponseDto;
import com.stackflov.entity.User;
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

    public void register(SignupRequestDto requestDto) {
        if (userRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());

        User user = User.builder()
                .email(requestDto.getEmail())
                .password(encodedPassword)
                .profileImage(requestDto.getProfileImage())
                .nickname(requestDto.getNickname())
                .socialType(requestDto.getSocialType())
                .socialId(requestDto.getSocialId())
                .level(requestDto.getLevel())
                .role(requestDto.getRole())
                .build();

        userRepository.save(user);
    }

    public TokenResponseDto login(LoginRequestDto requestDto) {

        // 이메일로 사용자 조회
        User user = userRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 비밀번호 검증
        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // JWT 토큰 생성
        String token = jwtProvider.createToken(user.getEmail(), user.getRole().name());

        return new TokenResponseDto(token);
    }
}
