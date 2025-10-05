package com.stackflov.controller;

import com.stackflov.dto.BoardListResponseDto;
import com.stackflov.dto.UserProfileDto;
import com.stackflov.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Profiles", description = "사용자 프로필 및 작성 게시글 조회 API")
@RestController
@RequestMapping("/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    // 사용자 프로필 정보 조회 API
    @Operation(summary = "프로필 조회", description = "userId에 해당하는 사용자의 프로필 정보를 조회합니다.")
    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileDto> getUserProfile(@PathVariable Long userId) {
        UserProfileDto userProfile = profileService.getUserProfile(userId);
        return ResponseEntity.ok(userProfile);
    }

    // 특정 사용자가 작성한 게시글 목록 조회 API
    @Operation(summary = "사용자 작성 게시글 조회", description = "지정한 사용자(userId)가 작성한 게시글을 최신순으로 페이징 조회합니다.")
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