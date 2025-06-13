package com.stackflov.repository;

import com.stackflov.domain.Follow;
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
}
