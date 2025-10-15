package com.stackflov.repository;

import com.stackflov.domain.Comment;
import com.stackflov.domain.User;
import com.stackflov.repository.projection.DailyStatProjection;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    long countByUser_Id(Long userId);
    List<Comment> findByBoardId(Long boardId);

    Optional<Comment> findByIdAndActiveTrue(Long id);

    List<Comment> findByBoardIdAndActiveTrue(Long boardId);

    long countByActiveTrue();

    @Query("SELECT c FROM Comment c WHERE c.content LIKE %:keyword% OR c.user.nickname LIKE %:keyword%")
    Page<Comment> searchAllBy(@Param("keyword") String keyword, Pageable pageable);

    Page<Comment> findByUser(User user, Pageable pageable);

    List<Comment> findByUser(User user);

    List<Comment> findByReviewIdAndActiveTrue(Long reviewId);

    Page<Comment> findByUserAndBoardIsNotNull(User user, Pageable pageable);

    Page<Comment> findByUserAndReviewIsNotNull(User user, Pageable pageable);

    @Modifying(clearAutomatically = true) // 벌크 연산 후 영속성 컨텍스트를 초기화
    @Query("UPDATE Comment c SET c.active = false WHERE c.id IN :ids")
    void bulkDeactivateByIds(@Param("ids") List<Long> ids);

    @Query("SELECT FUNCTION('DATE', c.createdAt) as date, COUNT(c.id) as count FROM Comment c WHERE c.createdAt >= :startDate GROUP BY date ORDER BY date")
    List<DailyStatProjection> countDailyComments(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT c FROM Comment c WHERE c.user = :user AND c.board IS NOT NULL AND c.active = true AND c.board.active = true")
    Page<Comment> findActiveByUserOnBoards(@Param("user") User user, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE c.user = :user AND c.review IS NOT NULL AND c.active = true AND c.review.active = true")
    Page<Comment> findActiveByUserOnReviews(@Param("user") User user, Pageable pageable);
}
