package com.stackflov.repository;

import com.stackflov.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // 👈 추가
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long>, JpaSpecificationExecutor<Review> { // 👈 상속 추가
    List<Review> findByLocationId(Long locationId);
}