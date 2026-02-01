package com.stackflov.service;

import com.stackflov.domain.Follow;
import com.stackflov.domain.NotificationType;
import com.stackflov.domain.Role;
import com.stackflov.domain.User;
import com.stackflov.dto.UserResponseDto;
import com.stackflov.repository.FollowRepository;
import com.stackflov.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional // 클래스 레벨에 붙여 기본적으로 적용 (읽기 전용은 별도 표시)
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final S3Service s3Service;

    /**
     * 팔로우 실행
     */
    public void follow(Long followerId, Long followedId) {
        if (Objects.equals(followerId, followedId)) {
            throw new IllegalArgumentException("자기 자신을 팔로우할 수 없습니다.");
        }

        User follower = findAndValidateUser(followerId);
        User followed = findAndValidateUser(followedId);

        validateNotAdmin(follower, "관리자 계정은 팔로우 기능을 사용할 수 없습니다.");
        validateNotAdmin(followed, "관리자는 팔로우 대상이 될 수 없습니다.");

        followRepository.findByFollowerIdAndFollowedId(followerId, followedId)
                .ifPresentOrElse(
                        follow -> {
                            if (follow.isActive()) throw new IllegalArgumentException("이미 팔로우한 사용자입니다.");
                            follow.activate(); // Dirty Checking으로 자동 저장
                        },
                        () -> {
                            followRepository.save(Follow.builder().follower(follower).followed(followed).build());
                            sendFollowNotification(follower, followed);
                        }
                );
    }

    /**
     * 언팔로우 실행
     */
    public void unfollow(Long followerId, Long followedId) {
        // 유저 정보는 이미 follow 객체 안에 포함되어 있으므로 추가 조회는 불필요할 수 있음
        Follow follow = followRepository.findByFollowerIdAndFollowedIdAndActiveTrue(followerId, followedId)
                .orElseThrow(() -> new IllegalArgumentException("팔로우 관계가 존재하지 않습니다."));

        // 관리자 체크가 꼭 필요하다면 유지
        validateNotAdmin(follow.getFollower(), "관리자 계정은 이 기능을 사용할 수 없습니다.");

        follow.deactivate(); // Dirty Checking
    }

    @Transactional(readOnly = true)
    public boolean isFollowing(Long followerId, Long followedId) {
        return followRepository.existsByFollowerIdAndFollowedIdAndActiveTrue(followerId, followedId);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDto> getFollowers(Long followedId) {
        return followRepository.findByFollowedIdAndActiveTrue(followedId).stream()
                .map(follow -> convertToDto(follow.getFollower()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserResponseDto> getFollowing(Long followerId) {
        return followRepository.findByFollowerIdAndActiveTrue(followerId).stream()
                .map(follow -> convertToDto(follow.getFollowed()))
                .collect(Collectors.toList());
    }

    /**
     * 탈퇴 시 모든 팔로우 관계 비활성화 (Bulk Update 권장)
     */
    public void deactivateAllFollowsByUser(User user) {
        // 리스트를 가져와서 하나씩 바꾸는 것보다 Repository에 쿼리를 만드는 것이 성능상 유리합니다.
        followRepository.deactivateAllByUserId(user.getId());
    }

    // --- Helper Methods ---

    private User findAndValidateUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다. ID: " + userId));
    }

    private void validateNotAdmin(User user, String message) {
        if (user.getRole() == Role.ADMIN) {
            throw new IllegalArgumentException(message);
        }
    }

    private void sendFollowNotification(User follower, User followed) {
        notificationService.notify(
                followed,
                NotificationType.FOLLOW,
                follower.getNickname() + "님이 나를 팔로우하기 시작했습니다.",
                "/profiles/" + follower.getId()
        );
    }

    private UserResponseDto convertToDto(User user) {
        String profileUrl = s3Service.publicUrl(user.getProfileImage());
        return new UserResponseDto(user, profileUrl);
    }
}