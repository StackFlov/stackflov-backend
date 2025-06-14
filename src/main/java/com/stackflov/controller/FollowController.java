package com.stackflov.controller;

import com.stackflov.service.FollowService;
import com.stackflov.dto.FollowRequestDto;
import com.stackflov.dto.UserResponseDto; // UserResponseDto 임포트 추가
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/follows")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    // 팔로우 추가
    @PostMapping("/follow")
    public ResponseEntity<String> follow(@RequestBody FollowRequestDto followRequestDto) {
        try {
            followService.follow(followRequestDto.getFollowerId(), followRequestDto.getFollowedId());
            return ResponseEntity.ok("팔로우 성공");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 팔로우 취소
    @DeleteMapping("/unfollow")
    public ResponseEntity<String> unfollow(@RequestBody FollowRequestDto followRequestDto) {
        try {
            followService.unfollow(followRequestDto.getFollowerId(), followRequestDto.getFollowedId());
            return ResponseEntity.ok("팔로우 취소 성공");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 팔로워 목록 조회 - 반환 타입을 List<UserResponseDto>로 변경
    @GetMapping("/followers/{followedId}")
    public ResponseEntity<List<UserResponseDto>> getFollowers(@PathVariable Long followedId) {
        List<UserResponseDto> followers = followService.getFollowers(followedId);
        return ResponseEntity.ok(followers);
    }

    // 팔로잉 목록 조회 - 반환 타입을 List<UserResponseDto>로 변경
    @GetMapping("/following/{followerId}")
    public ResponseEntity<List<UserResponseDto>> getFollowing(@PathVariable Long followerId) {
        List<UserResponseDto> following = followService.getFollowing(followerId);
        return ResponseEntity.ok(following);
    }
}