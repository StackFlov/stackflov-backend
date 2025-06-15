package com.stackflov.service;

import com.stackflov.domain.Role;
import com.stackflov.domain.SocialType;
import com.stackflov.domain.User;
import com.stackflov.dto.LoginRequestDto;
import com.stackflov.dto.PasswordUpdateRequestDto;
import com.stackflov.dto.SignupRequestDto;
import com.stackflov.dto.TokenResponseDto;
import com.stackflov.dto.UserResponseDto;
import com.stackflov.dto.UserUpdateRequestDto;
import com.stackflov.jwt.JwtProvider;
import com.stackflov.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RedisService redisService;
    private final BoardService boardService;
    private final CommentService commentService; // ✅ CommentService 주입

    // 회원가입
    @Transactional
    public UserResponseDto register(SignupRequestDto signupRequestDto) {
        String email = signupRequestDto.getEmail();

        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다. 다른 이메일을 사용해주세요.");
        }

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

        User savedUser = userRepository.save(user);

        return new UserResponseDto(savedUser);
    }

    // 로그인
    @Transactional(readOnly = true)
    public TokenResponseDto login(LoginRequestDto loginRequestDto) {
        User user = userRepository.findByEmail(loginRequestDto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("이메일을 찾을 수 없습니다."));

        if (!user.isActive()) {
            throw new IllegalArgumentException("비활성화된 계정입니다. 관리자에게 문의하세요.");
        }

        if (!passwordEncoder.matches(loginRequestDto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        String accessToken = jwtProvider.createAccessToken(user.getEmail(), user.getRole().name());
        String refreshToken = jwtProvider.createRefreshToken(user.getEmail());

        redisService.save("RT:" + user.getEmail(), refreshToken, jwtProvider.REFRESH_TOKEN_EXPIRE_TIME);

        return new TokenResponseDto(accessToken, refreshToken);
    }

    // 비밀번호 변경
    @Transactional
    public void updatePassword(String userEmail, PasswordUpdateRequestDto dto) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        if (passwordEncoder.matches(dto.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("새 비밀번호는 현재 비밀번호와 다르게 설정해야 합니다.");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
    }

    // 내 정보 조회 (이메일로 사용자 조회)
    @Transactional(readOnly = true)
    public UserResponseDto getUserByEmail(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return new UserResponseDto(user);
    }

    // 사용자 정보 업데이트 (닉네임, 프로필 이미지 등)
    @Transactional
    public void updateUser(String userEmail, UserUpdateRequestDto dto) {
        User user = userRepository.findByEmailAndActiveTrue(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 비활성화된 사용자입니다."));

        if (dto.getNickname() != null && !dto.getNickname().isEmpty()) {
            user.updateNickname(dto.getNickname());
        }
        if (dto.getProfileImage() != null && !dto.getProfileImage().isEmpty()) {
            user.updateProfileImage(dto.getProfileImage());
        }
    }

    // --- 회원 비활성화/활성화 기능 ---
    @Transactional
    public void deactivateUser(String targetUserEmail) {
        User user = userRepository.findByEmail(targetUserEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (!user.isActive()) {
            throw new IllegalArgumentException("이미 비활성화된 사용자입니다.");
        }

        user.deactivate();
        redisService.delete("RT:" + user.getEmail());
        boardService.deactivateBoardsByUser(user); // 이 사용자의 모든 게시글 비활성화
        commentService.deactivateCommentsByUser(user); // ✅ 이 사용자의 모든 댓글 비활성화
    }

    @Transactional
    public void activateUser(String targetUserEmail) {
        User user = userRepository.findByEmail(targetUserEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (user.isActive()) {
            throw new IllegalArgumentException("이미 활성화된 사용자입니다.");
        }

        user.activate();
        boardService.activateBoardsByUser(user); // 이 사용자의 모든 게시글 활성화
        commentService.activateCommentsByUser(user); // ✅ 이 사용자의 모든 댓글 활성화
    }
}