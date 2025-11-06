package com.stackflov.repository;

import com.stackflov.domain.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {
    List<ReviewImage> findAllByReviewId(Long reviewId);
    List<ReviewImage> findAllByIdInAndReviewId(Collection<Long> ids, Long reviewId);
    long countByReviewId(Long reviewId);
}