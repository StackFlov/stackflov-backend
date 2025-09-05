package com.stackflov.repository;

import com.stackflov.domain.Follow;
import com.stackflov.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    Optional<Follow> findByFollowerIdAndFollowedId(Long followerId, Long followedId);

    // ⬇ 소프트 삭제 대응 (활성 기준)
    Optional<Follow> findByFollowerIdAndFollowedIdAndActiveTrue(Long followerId, Long followedId);

    List<Follow> findByFollowerId(Long followerId);
    List<Follow> findByFollowedId(Long followedId);

    // ⬇ 소프트 삭제 대응 (활성 기준 리스트/카운트)
    List<Follow> findByFollowerIdAndActiveTrue(Long followerId);
    List<Follow> findByFollowedIdAndActiveTrue(Long followedId);

    List<Follow> findByFollower(User follower);
    List<Follow> findByFollowed(User followed);

    long countByFollowedId(Long followedId);
    long countByFollowerId(Long followerId);

    // ⬇ 소프트 삭제 대응 (활성 카운트)
    long countByFollowedIdAndActiveTrue(Long followedId);
    long countByFollowerIdAndActiveTrue(Long followerId);
}
