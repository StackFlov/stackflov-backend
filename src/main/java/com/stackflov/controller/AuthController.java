package com.stackflov.controller;

import com.stackflov.dto.*;
import com.stackflov.service.AuthService;
import com.stackflov.service.EmailService;
import com.stackflov.service.SmsService;
import com.stackflov.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
@Tag(
        name = "Auth",
        description = "회원가입/로그인, 토큰 재발급, 이메일·문자 인증 등 인증 관련 API"
)
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final EmailService emailService;
    private final SmsService smsService;

    @Operation(summary = "액세스 토큰 재발급", description = "리프레시 토큰을 이용해 새로운 액세스 토큰을 발급합니다.")
    @PostMapping("/reissue")
    public ResponseEntity<TokenResponseDto> reissue(@RequestBody ReissueRequest request) {
        try {
            TokenResponseDto tokenResponse = authService.reissueToken(request.getRefreshToken());
            return ResponseEntity.ok(tokenResponse);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @Operation(summary = "회원가입", description = "이메일/비밀번호 및 기본 정보를 이용해 새 계정을 생성합니다.")
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody SignupRequestDto signupRequestDto) {
        userService.register(signupRequestDto);
        return ResponseEntity.ok("회원가입 성공");
    }

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하고 액세스/리프레시 토큰을 발급합니다.")
    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> login(@RequestBody LoginRequestDto loginRequestDto) {
        try {
            TokenResponseDto tokens = userService.login(loginRequestDto);
            return ResponseEntity.ok(tokens);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }

    @Operation(summary = "로그아웃", description = "현재 로그인한 사용자의 세션/토큰을 무효화합니다.")
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@AuthenticationPrincipal String email) {
        authService.logout(email);
        return ResponseEntity.ok("로그아웃 완료");
    }

    @Operation(summary = "이메일 인증코드 전송", description = "입력한 이메일로 인증 코드를 발송합니다.")
    @PostMapping("/email/send")
    public ResponseEntity<String> sendCode(@RequestBody EmailRequestDto requestDto) {
        emailService.sendVerificationCode(requestDto.getEmail());
        return ResponseEntity.ok("인증 코드가 이메일로 전송되었습니다.");
    }

    @Operation(summary = "이메일 인증코드 검증", description = "수신한 이메일 인증 코드를 검증합니다.")
    @PostMapping("/email/verify")
    public ResponseEntity<String> verifyCode(@RequestBody EmailCodeVerifyRequestDto dto) {
        boolean result = emailService.verifyCode(dto.getEmail(), dto.getCode());
        return result
                ? ResponseEntity.ok("이메일 인증 성공")
                : ResponseEntity.status(HttpStatus.BAD_REQUEST).body("이메일 인증 실패");
    }

    @Operation(summary = "휴대폰 인증번호 전송", description = "입력한 휴대폰 번호로 인증번호를 발송합니다.")
    @PostMapping("/phone/send")
    public ResponseEntity<String> sendSmsCode(@RequestBody PhoneRequestDto requestDto) {
        smsService.sendVerificationCode(requestDto.getPhoneNumber());
        return ResponseEntity.ok("인증번호가 발송되었습니다.");
    }

    @Operation(summary = "휴대폰 인증번호 검증", description = "수신한 휴대폰 인증번호를 검증합니다.")
    @PostMapping("/phone/verify")
    public ResponseEntity<String> verifySmsCode(@RequestBody PhoneVerifyRequestDto requestDto) {
        boolean isVerified = smsService.verifyCode(requestDto.getPhoneNumber(), requestDto.getCode());
        if (isVerified) {
            return ResponseEntity.ok("전화번호 인증에 성공했습니다.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("인증번호가 일치하지 않습니다.");
        }
    }
}

@Getter
class ReissueRequest {
    private String refreshToken;
}
