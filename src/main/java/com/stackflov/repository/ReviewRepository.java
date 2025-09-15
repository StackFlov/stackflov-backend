package com.stackflov.repository;

import com.stackflov.domain.Review;
import com.stackflov.repository.projection.DailyStatProjection;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // 👈 추가

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.stackflov.domain.User;
import org.springframework.data.jpa.repository.Query;

public interface ReviewRepository extends JpaRepository<Review, Long>, JpaSpecificationExecutor<Review> { // 👈 상속 추가
    List<Review> findByLocationId(Long locationId);
    Page<Review> findByAuthor(User author, Pageable pageable);
    @Query("SELECT FUNCTION('DATE', r.createdAt) as date, COUNT(r.id) as count FROM Review r WHERE r.createdAt >= :startDate GROUP BY date ORDER BY date")
    List<DailyStatProjection> countDailyReviews(@Param("startDate") LocalDateTime startDate);
}