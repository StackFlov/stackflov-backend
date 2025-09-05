package com.stackflov.controller;

import com.stackflov.dto.*;
import com.stackflov.service.AuthService;
import com.stackflov.service.EmailService;
import com.stackflov.service.SmsService;
import com.stackflov.service.UserService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final EmailService emailService;
    private final SmsService smsService;

    @PostMapping("/reissue")
    public ResponseEntity<TokenResponseDto> reissue(@RequestBody ReissueRequest request) {
        try {
            TokenResponseDto tokenResponse = authService.reissueToken(request.getRefreshToken());
            return ResponseEntity.ok(tokenResponse);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody SignupRequestDto signupRequestDto) {
        userService.register(signupRequestDto);
        return ResponseEntity.ok("회원가입 성공");
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> login(@RequestBody LoginRequestDto loginRequestDto) {
        try {
            TokenResponseDto tokens = userService.login(loginRequestDto);
            return ResponseEntity.ok(tokens);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@AuthenticationPrincipal String email) {
        authService.logout(email);
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

    @PostMapping("/phone/send")
    public ResponseEntity<String> sendSmsCode(@RequestBody PhoneRequestDto requestDto) {
        smsService.sendVerificationCode(requestDto.getPhoneNumber());
        return ResponseEntity.ok("인증번호가 발송되었습니다.");
    }

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
