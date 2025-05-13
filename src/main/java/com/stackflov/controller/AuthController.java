package com.stackflov.controller;

import com.stackflov.dto.LoginRequestDto;
import com.stackflov.dto.SignupRequestDto;
import com.stackflov.dto.TokenResponseDto;
import com.stackflov.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> login(@RequestBody LoginRequestDto requestDto) {
        TokenResponseDto response = userService.login(requestDto);
        return ResponseEntity.ok(response);
    }

    // 회원가입
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody SignupRequestDto requestDto) {
        userService.register(requestDto);
        return ResponseEntity.ok("회원가입 완료");
    }
}
