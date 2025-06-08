package com.stackflov.controller;

import com.stackflov.dto.UserResponseDto;
import com.stackflov.jwt.JwtProvider;
import com.stackflov.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users") // 이것도 빠져 있었을 수 있음
@RequiredArgsConstructor
public class UserController {
    private final JwtProvider jwtProvider;
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getMyInfo(@RequestHeader("Authorization") String accessToken) {
        String token = accessToken.replace("Bearer ", "");
        String email = jwtProvider.getEmail(token);

        UserResponseDto dto = userService.getUserByEmail(email);
        return ResponseEntity.ok(dto);
    }
}
