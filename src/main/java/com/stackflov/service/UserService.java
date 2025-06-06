package com.stackflov.service;

import com.stackflov.domain.Role;
import com.stackflov.domain.SocialType;
import com.stackflov.domain.User;
import com.stackflov.dto.LoginRequestDto;
import com.stackflov.dto.SignupRequestDto;
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
    public void register(SignupRequestDto signupRequestDto) {
        // 이메일 중복 체크
        if (userRepository.findByEmail(signupRequestDto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        // 사용자 엔티티 생성
        User user = User.builder()
                .email(signupRequestDto.getEmail())
                // 비밀번호는 암호화해서 저장
                .password(passwordEncoder.encode(signupRequestDto.getPassword()))
                .nickname(signupRequestDto.getNickname())
                .profileImage(signupRequestDto.getProfileImage())
                .socialType(signupRequestDto.getSocialType() != null ? signupRequestDto.getSocialType() : SocialType.NONE)
                .socialId(signupRequestDto.getSocialId())
                .level(signupRequestDto.getLevel())
                .role(signupRequestDto.getRole() != null ? signupRequestDto.getRole() : Role.USER)
                .build();

        // DB 저장
        userRepository.save(user);
    }
}
