package com.stackflov.controller;

import com.stackflov.domain.User;
import com.stackflov.service.FollowService;
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
    public ResponseEntity<String> follow(@RequestParam Long followerId, @RequestParam Long followedId) {
        try {
            followService.follow(followerId, followedId);
            return ResponseEntity.ok("팔로우 성공");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 팔로우 취소
    @DeleteMapping("/unfollow")
    public ResponseEntity<String> unfollow(@RequestParam Long followerId, @RequestParam Long followedId) {
        try {
            followService.unfollow(followerId, followedId);
            return ResponseEntity.ok("팔로우 취소 성공");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 팔로워 목록 조회
    @GetMapping("/followers/{followedId}")
    public ResponseEntity<List<User>> getFollowers(@PathVariable Long followedId) {
        List<User> followers = followService.getFollowers(followedId);
        return ResponseEntity.ok(followers);
    }

    // 팔로잉 목록 조회
    @GetMapping("/following/{followerId}")
    public ResponseEntity<List<User>> getFollowing(@PathVariable Long followerId) {
        List<User> following = followService.getFollowing(followerId);
        return ResponseEntity.ok(following);
    }
}
