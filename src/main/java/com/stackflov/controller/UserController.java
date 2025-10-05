package com.stackflov.controller;

import com.stackflov.dto.PasswordUpdateRequestDto;
import com.stackflov.dto.UserResponseDto;
import com.stackflov.dto.UserUpdateRequestDto;
import com.stackflov.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.stackflov.config.CustomUserPrincipal;

@Tag(name = "Users", description = "내 정보 조회/수정 및 비밀번호 변경 API")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 프로필 정보를 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getMyInfo(@AuthenticationPrincipal CustomUserPrincipal principal) {
        UserResponseDto dto = userService.getUserByEmail(principal.getEmail());
        return ResponseEntity.ok(dto);
        }

    @Operation(summary = "내 정보 수정", description = "현재 로그인한 사용자의 프로필 정보를 수정합니다.")
    @PutMapping("/me")
    public ResponseEntity<?> updateMyInfo(
            @RequestBody UserUpdateRequestDto dto,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        userService.updateUser(principal.getEmail(), dto);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "비밀번호 변경", description = "현재 로그인한 사용자의 비밀번호를 변경합니다.")
    @PutMapping("/password")
    public ResponseEntity<Void> updatePassword(@RequestBody PasswordUpdateRequestDto dto,
                                               @AuthenticationPrincipal CustomUserPrincipal principal) {
        userService.updatePassword(principal.getEmail(), dto);
        return ResponseEntity.ok().build();
    }
}
