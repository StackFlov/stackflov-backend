package com.stackflov.repository;

import com.stackflov.domain.Follow;
import com.stackflov.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    // 특정 유저가 다른 유저를 팔로우하는지 확인
    Optional<Follow> findByFollowerIdAndFollowedId(Long followerId, Long followedId);

    // 특정 유저가 팔로우한 모든 유저 조회
    List<Follow> findByFollowerId(Long followerId);

    // 특정 유저를 팔로우하는 모든 유저 조회
    List<Follow> findByFollowedId(Long followedId);
    // [추가] 특정 사용자가 팔로우하는 관계 조회 (내가 남을)
    List<Follow> findByFollower(User follower);

    // [추가] 특정 사용자를 팔로우하는 관계 조회 (남이 나를)
    List<Follow> findByFollowed(User followed);

    long countByFollowedId(Long followedId); // 나를 팔로우하는 사람(팔로워) 수
    long countByFollowerId(Long followerId);
}
