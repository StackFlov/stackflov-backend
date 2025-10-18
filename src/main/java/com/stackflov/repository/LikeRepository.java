package com.stackflov.repository;

import com.stackflov.domain.Board;
import com.stackflov.domain.Like;
import com.stackflov.domain.Review;
import com.stackflov.domain.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {

    Optional<Like> findByUserAndBoard(User user, Board board);

    long countByBoard(Board board);

    boolean existsByUserAndBoard(User user, Board board);

    // ⬇ 소프트 삭제 대응 (활성 기준)
    Optional<Like> findByUserAndBoardAndActiveTrue(User user, Board board);
    long countByBoardAndActiveTrue(Board board);
    boolean existsByUserAndBoardAndActiveTrue(User user, Board board);

    List<Like> findByBoard(Board board);

    Optional<Like> findByUserAndReview(User user, Review review);
    Optional<Like> findByUserAndReviewAndActiveTrue(User user, Review review);
    @Query("""
           select l.review.id
           from Like l
           where l.active = true
             and l.user.id = :userId
             and l.review.id in :reviewIds
           """)
    List<Long> findLikedReviewIds(@Param("userId") Long userId,
                                  @Param("reviewIds") List<Long> reviewIds);

    // 리뷰별 좋아요 수 집계
    interface ReviewLikeCount {
        Long getReviewId();
        Long getCnt();
    }

    @Query("""
           select l.review.id as reviewId, count(l) as cnt
           from Like l
           where l.active = true
             and l.review.id in :reviewIds
           group by l.review.id
           """)
    List<ReviewLikeCount> countActiveLikesByReviewIds(@Param("reviewIds") List<Long> reviewIds);
}
