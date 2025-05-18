package com.stackflov.controller;

import com.stackflov.dto.LoginRequestDto;
import com.stackflov.dto.SignupRequestDto;
import com.stackflov.dto.TokenResponseDto;
import com.stackflov.service.AuthService;
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
}

// DTO for refresh token request
@Getter
class ReissueRequest {
    private String refreshToken;
}
