package com.stackflov.controller;

import com.stackflov.dto.PasswordUpdateRequestDto;
import com.stackflov.dto.UserResponseDto;
import com.stackflov.dto.UserUpdateRequestDto;
import com.stackflov.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.stackflov.config.CustomUserPrincipal;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getMyInfo(@AuthenticationPrincipal CustomUserPrincipal principal) {
        UserResponseDto dto = userService.getUserByEmail(principal.getEmail());
        return ResponseEntity.ok(dto);
        }

    @PutMapping("/me")
    public ResponseEntity<?> updateMyInfo(
            @RequestBody UserUpdateRequestDto dto,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        userService.updateUser(principal.getEmail(), dto);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/password")
    public ResponseEntity<Void> updatePassword(@RequestBody PasswordUpdateRequestDto dto,
                                               @AuthenticationPrincipal CustomUserPrincipal principal) {
        userService.updatePassword(principal.getEmail(), dto);
        return ResponseEntity.ok().build();
    }
}
