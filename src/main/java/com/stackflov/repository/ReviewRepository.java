package com.stackflov.repository;

import com.stackflov.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // 👈 추가
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.stackflov.domain.User;

public interface ReviewRepository extends JpaRepository<Review, Long>, JpaSpecificationExecutor<Review> { // 👈 상속 추가
    List<Review> findByLocationId(Long locationId);
    Page<Review> findByAuthor(User author, Pageable pageable);
}