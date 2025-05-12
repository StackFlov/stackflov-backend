package com.stackflov.controller;

import com.stackflov.dto.LoginRequestDto;
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
}
