package com.stackflov.service;

import com.stackflov.domain.Follow;
import com.stackflov.domain.User;
import com.stackflov.repository.FollowRepository;
import com.stackflov.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    // 팔로우 추가
    @Transactional
    public void follow(Long followerId, Long followedId) {
        // 팔로우하려는 유저와 팔로우 당하는 유저 조회
        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new IllegalArgumentException("팔로우하는 사용자가 존재하지 않습니다."));
        User followed = userRepository.findById(followedId)
                .orElseThrow(() -> new IllegalArgumentException("팔로우 당하는 사용자가 존재하지 않습니다."));

        // 중복 팔로우 방지
        if (followRepository.findByFollowerIdAndFollowedId(followerId, followedId).isPresent()) {
            throw new IllegalArgumentException("이미 팔로우한 사용자입니다.");
        }

        // 팔로우 관계 저장
        Follow follow = Follow.builder()
                .follower(follower)
                .followed(followed)
                .build();

        followRepository.save(follow);
    }

    // 팔로우 취소
    @Transactional
    public void unfollow(Long followerId, Long followedId) {
        Follow follow = followRepository.findByFollowerIdAndFollowedId(followerId, followedId)
                .orElseThrow(() -> new IllegalArgumentException("팔로우 관계가 존재하지 않습니다."));

        followRepository.delete(follow);
    }

    // 팔로우 상태 확인
    public boolean isFollowing(Long followerId, Long followedId) {
        return followRepository.findByFollowerIdAndFollowedId(followerId, followedId).isPresent();
    }

    // 팔로워 목록 조회
    public List<User> getFollowers(Long followedId) {
        List<Follow> follows = followRepository.findByFollowedId(followedId);
        return follows.stream().map(follow -> follow.getFollower()).collect(Collectors.toList());
    }

    // 팔로우 목록 조회
    public List<User> getFollowing(Long followerId) {
        List<Follow> follows = followRepository.findByFollowerId(followerId);
        return follows.stream().map(follow -> follow.getFollowed()).collect(Collectors.toList());
    }
}
