package com.stackflov.controller;

import com.stackflov.dto.PasswordUpdateRequestDto;
import com.stackflov.dto.UserResponseDto;
import com.stackflov.dto.UserUpdateRequestDto;
import com.stackflov.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getMyInfo(@AuthenticationPrincipal String email) {
        UserResponseDto dto = userService.getUserByEmail(email);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateMyInfo(
            @RequestBody UserUpdateRequestDto dto,
            @AuthenticationPrincipal String email) {
        userService.updateUser(email, dto);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/password")
    public ResponseEntity<Void> updatePassword(@RequestBody PasswordUpdateRequestDto dto,
                                               @AuthenticationPrincipal String email) {
        userService.updatePassword(email, dto);
        return ResponseEntity.ok().build();
    }
}
