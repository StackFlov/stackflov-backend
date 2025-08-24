package com.stackflov.controller;

import com.stackflov.dto.BoardListResponseDto;
import com.stackflov.dto.UserProfileDto;
import com.stackflov.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    // 사용자 프로필 정보 조회 API
    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileDto> getUserProfile(@PathVariable Long userId) {
        UserProfileDto userProfile = profileService.getUserProfile(userId);
        return ResponseEntity.ok(userProfile);
    }

    // 특정 사용자가 작성한 게시글 목록 조회 API
    @GetMapping("/{userId}/boards")
    public ResponseEntity<Page<BoardListResponseDto>> getBoardsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<BoardListResponseDto> boards = profileService.getBoardsByUser(userId, pageable);
        return ResponseEntity.ok(boards);
    }
}