package com.stackflov.controller;

import com.stackflov.dto.PasswordUpdateRequestDto;
import com.stackflov.dto.UserResponseDto;
import com.stackflov.dto.UserUpdateRequestDto;
import com.stackflov.jwt.JwtProvider;
import com.stackflov.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final JwtProvider jwtProvider;
    private final UserService userService;

    // 내 정보 조회
    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getMyInfo(@RequestHeader("Authorization") String accessToken) {
        String token = accessToken.replace("Bearer ", "");
        String email = jwtProvider.getEmail(token);

        UserResponseDto dto = userService.getUserByEmail(email);
        return ResponseEntity.ok(dto);
    }

    // 내 정보 업데이트 (닉네임, 프로필 이미지)
    @PutMapping("/me")
    public ResponseEntity<?> updateMyInfo(
            @RequestBody UserUpdateRequestDto dto,
            @RequestHeader("Authorization") String accessToken) {
        String token = accessToken.replace("Bearer ", "");
        String email = jwtProvider.getEmail(token);
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

    // --- 사용자 계정 비활성화/활성화 API (수정: 타인 계정 비활성화/활성화 제거) ---

    // ✅ 본인 계정 비활성화 API
    @PutMapping("/me/deactivate")
    public ResponseEntity<String> deactivateMyAccount(
            @RequestAttribute("email") String userEmail) { // 자신의 이메일 (토큰에서 추출)
        try {
            userService.deactivateUser(userEmail); // 자신의 이메일을 인자로 전달
            return ResponseEntity.ok("회원님의 계정(" + userEmail + ")이 성공적으로 비활성화되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    // --- 사용자 계정 비활성화/활성화 API 끝 ---
}