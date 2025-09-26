package com.stackflov.service;

import com.stackflov.domain.Follow;
import com.stackflov.domain.NotificationType;
import com.stackflov.domain.Role;
import com.stackflov.domain.User;
import com.stackflov.dto.UserResponseDto;
import com.stackflov.repository.FollowRepository;
import com.stackflov.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public void follow(Long followerId, Long followedId) {
        if (Objects.equals(followerId, followedId)) {
            throw new IllegalArgumentException("자기 자신을 팔로우할 수 없습니다.");
        }
        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new IllegalArgumentException("팔로우하는 사용자가 존재하지 않습니다."));
        User followed = userRepository.findById(followedId)
                .orElseThrow(() -> new IllegalArgumentException("팔로우 당하는 사용자가 존재하지 않습니다."));

        if (follower.getRole() == Role.ADMIN) {
            throw new IllegalArgumentException("관리자 계정은 팔로우 기능을 사용할 수 없습니다.");
        }
        if (followed.getRole() == Role.ADMIN) {
            throw new IllegalArgumentException("관리자는 팔로우할 수 없습니다.");
        }

        Optional<Follow> existing = followRepository.findByFollowerIdAndFollowedId(followerId, followedId);
        if (existing.isPresent()) {
            Follow follow = existing.get();
            if (follow.isActive()) {
                throw new IllegalArgumentException("이미 팔로우한 사용자입니다.");
            }
            follow.activate();               // 👈 리액티베이션
            followRepository.save(follow);
            return;
        }

        followRepository.save(Follow.builder().follower(follower).followed(followed).build());

        if (!follower.getId().equals(followed.getId())) {
            notificationService.notify(
                    followed,
                    NotificationType.FOLLOW,
                    follower.getNickname() + "님이 나를 팔로우하기 시작했습니다.",
                    "/profiles/" + follower.getId()
            );
        }
    }

    @Transactional
    public void unfollow(Long followerId, Long followedId) {
        Follow follow = followRepository.findByFollowerIdAndFollowedIdAndActiveTrue(followerId, followedId)
                .orElseThrow(() -> new IllegalArgumentException("팔로우 관계가 존재하지 않습니다."));

        User followed = userRepository.findById(followedId)
                .orElseThrow(() -> new IllegalArgumentException("언팔로우 당하는 사용자가 존재하지 않습니다."));

        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new IllegalArgumentException("언팔로우 당하는 사용자가 존재하지 않습니다."));

        if (follower.getRole() == Role.ADMIN) {
            throw new IllegalArgumentException("관리자 계정은 팔로우 기능을 사용할 수 없습니다.");
        }
        if (followed.getRole() == Role.ADMIN) {
            throw new IllegalArgumentException("관리자는 언팔로우 대상이 아닙니다.");
        }
        if (!follow.isActive()) {
            throw new IllegalArgumentException("이미 언팔로우된 사용자입니다.");
        }
        follow.deactivate();
        followRepository.save(follow);
    }

    public boolean isFollowing(Long followerId, Long followedId) {
        return followRepository.findByFollowerIdAndFollowedIdAndActiveTrue(followerId, followedId).isPresent();
    }

    public List<UserResponseDto> getFollowers(Long followedId) {
        return followRepository.findByFollowedIdAndActiveTrue(followedId).stream()
                .map(f -> new UserResponseDto(f.getFollower()))
                .collect(Collectors.toList());
    }

    public List<UserResponseDto> getFollowing(Long followerId) {
        return followRepository.findByFollowerIdAndActiveTrue(followerId).stream()
                .map(f -> new UserResponseDto(f.getFollowed()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void deactivateAllFollowsByUser(User user) {
        followRepository.findByFollower(user).forEach(Follow::deactivate);
        followRepository.findByFollowed(user).forEach(Follow::deactivate);
    }
}
