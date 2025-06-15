package com.stackflov.controller;

import com.stackflov.dto.*;
import com.stackflov.jwt.JwtProvider;
import com.stackflov.service.AuthService;
import com.stackflov.service.EmailService;
import com.stackflov.service.RedisService;
import com.stackflov.service.UserService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final EmailService emailService;

    @PostMapping("/reissue")
    public ResponseEntity<TokenResponseDto> reissue(@RequestBody ReissueRequest request) {
        System.out.println("[DEBUG] /auth/reissue 요청 들어옴, refreshToken: " + request.getRefreshToken());
        try {
            TokenResponseDto tokenResponse = authService.reissueToken(request.getRefreshToken());
            return ResponseEntity.ok(tokenResponse);
        } catch (IllegalArgumentException e) {
            System.out.println("[ERROR] 토큰 재발급 실패: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }


    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody SignupRequestDto signupRequestDto) {
        userService.register(signupRequestDto);
        return ResponseEntity.ok("회원가입 성공");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto loginRequestDto) { // ResponseEntity<?> 로 변경
        try {
            TokenResponseDto tokens = userService.login(loginRequestDto);
            return ResponseEntity.ok(tokens);
        } catch (IllegalArgumentException e) {
            // ✅ 수정된 부분: 400 Bad Request와 함께 에러 메시지를 반환
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String accessToken) {
        String token = accessToken.replace("Bearer ", "");
        authService.logout(token);
        return ResponseEntity.ok("로그아웃 완료");
    }
    @PostMapping("/email/send")
    public ResponseEntity<String> sendCode(@RequestBody EmailRequestDto requestDto) {
        emailService.sendVerificationCode(requestDto.getEmail());
        return ResponseEntity.ok("인증 코드가 이메일로 전송되었습니다.");
    }

    @PostMapping("/email/verify")
    public ResponseEntity<String> verifyCode(@RequestBody EmailCodeVerifyRequestDto dto) {
        boolean result = emailService.verifyCode(dto.getEmail(), dto.getCode());
        return result
                ? ResponseEntity.ok("이메일 인증 성공")
                : ResponseEntity.status(HttpStatus.BAD_REQUEST).body("이메일 인증 실패");
    }

}

// DTO for refresh token request
@Getter
class ReissueRequest {
    private String refreshToken;
}
