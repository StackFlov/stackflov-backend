package com.stackflov.controller;

import com.stackflov.config.CustomUserPrincipal;
import com.stackflov.service.FollowService;
import com.stackflov.dto.FollowRequestDto;
import com.stackflov.dto.UserResponseDto; // UserResponseDto 임포트 추가
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Follows", description = "팔로우/언팔로우 및 팔로워·팔로잉 조회 API")
@RestController
@RequestMapping("/follows")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    // 팔로우 추가
    @Operation(summary = "팔로우 추가", description = "현재 로그인한 사용자가 지정한 사용자(followedId)를 팔로우합니다.")
    @PostMapping("/follow")
    public ResponseEntity<String> follow(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody FollowRequestDto dto) {
        try {
            followService.follow(principal.getId(), dto.getFollowedId());
            return ResponseEntity.ok("팔로우 성공");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 팔로우 취소
    @Operation(summary = "팔로우 취소", description = "현재 로그인한 사용자가 지정한 사용자(followedId)에 대한 팔로우를 취소합니다.")
    @DeleteMapping("/{followedId}")
    public ResponseEntity<String> unfollow(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long followedId) {
        try {
            followService.unfollow(principal.getId(), followedId);
            return ResponseEntity.ok("팔로우 취소 성공");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 팔로워 목록 조회 - 반환 타입을 List<UserResponseDto>로 변경
    //followedId를 팔로우 하는 사람들 조회
    @Operation(summary = "팔로워 목록 조회", description = "특정 사용자(followedId)를 팔로우하는 사용자 목록을 조회합니다.")
    @GetMapping("/followers/{followedId}")
    public ResponseEntity<List<UserResponseDto>> getFollowers(@PathVariable Long followedId) {
        List<UserResponseDto> followers = followService.getFollowers(followedId);
        return ResponseEntity.ok(followers);
    }

    // 팔로잉 목록 조회 - 반환 타입을 List<UserResponseDto>로 변경

    @Operation(summary = "팔로잉 목록 조회", description = "특정 사용자(followerId)가 팔로우 중인 사용자 목록을 조회합니다.")
    @GetMapping("/following/{followerId}")
    public ResponseEntity<List<UserResponseDto>> getFollowing(@PathVariable Long followerId) {
        List<UserResponseDto> following = followService.getFollowing(followerId);
        return ResponseEntity.ok(following);
    }
}