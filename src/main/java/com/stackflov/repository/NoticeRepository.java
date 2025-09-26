package com.stackflov.repository;

import com.stackflov.domain.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
    // 모든 공지사항을 최신순으로 페이징하여 조회
    Page<Notice> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Notice> findAllByActiveTrue(Pageable pageable);

    Optional<Notice> findByIdAndActiveTrue(Long id);
}