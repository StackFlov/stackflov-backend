package com.stackflov.controller;

import com.stackflov.dto.PasswordUpdateRequestDto;
import com.stackflov.dto.UserResponseDto;
import com.stackflov.dto.UserUpdateRequestDto;
import com.stackflov.jwt.JwtProvider;
import com.stackflov.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
    @PutMapping("/users/me")
    public ResponseEntity<?> updateMyInfo(
            @RequestBody UserUpdateRequestDto dto,
            @AuthenticationPrincipal String email) {
        userService.updateUser(email, dto);
        return ResponseEntity.ok().build();
    }
    // 비밀번호 변경 API
    @PutMapping("/password")
    public ResponseEntity<Void> updatePassword(@RequestBody PasswordUpdateRequestDto dto,
                                               @RequestAttribute("email") String email) {
        userService.updatePassword(email, dto);
        return ResponseEntity.ok().build();
    }

}
