package com.stackflov.service;

import com.stackflov.domain.Role;
import com.stackflov.domain.SocialType;
import com.stackflov.domain.User;
import com.stackflov.dto.*;
import com.stackflov.jwt.JwtProvider;
import com.stackflov.repository.UserRepository;
import jakarta.transaction.Transactional;
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
        String email = signupRequestDto.getEmail();

/*
        // ✅ 이메일 인증 여부 확인
        String verified = redisService.get("EMAIL_VERIFIED:" + email);
        if (!"true".equals(verified)) {
            throw new IllegalArgumentException("이메일 인증이 필요합니다.");
        }

        // ✅ 중복 이메일 확인
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }
*/
        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(signupRequestDto.getPassword()))
                .nickname(signupRequestDto.getNickname())
                .profileImage(signupRequestDto.getProfileImage())
                .socialType(signupRequestDto.getSocialType() != null ? signupRequestDto.getSocialType() : SocialType.NONE)
                .socialId(signupRequestDto.getSocialId())
                .level(signupRequestDto.getLevel())
                .role(signupRequestDto.getRole() != null ? signupRequestDto.getRole() : Role.USER)
                .build();

        userRepository.save(user);
    }
    public UserResponseDto getUserByEmail(String email) {
        User user = userRepository.findByEmailAndActiveTrue(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 탈퇴한 사용자입니다."));
        return new UserResponseDto(user);
    }
    @Transactional
    public void updateUser(String email, UserUpdateRequestDto dto) {
        User user = userRepository.findByEmailAndActiveTrue(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 탈퇴한 사용자입니다."));

        if (dto.getNickname() != null) {
            user.updateNickname(dto.getNickname());
        }

        if (dto.getProfileImage() != null) {
            user.updateProfileImage(dto.getProfileImage());
        }
    }
    @Transactional
    public void updatePassword(String email, PasswordUpdateRequestDto dto) {
        // 사용자 확인
        User user = userRepository.findByEmailAndActiveTrue(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 탈퇴한 사용자입니다."));

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 새 비밀번호로 변경
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
    }
}
