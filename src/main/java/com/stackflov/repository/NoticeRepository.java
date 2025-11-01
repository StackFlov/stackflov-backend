package com.stackflov.repository;

import com.stackflov.domain.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
    Page<Notice> findAllByOrderByCreatedAtDesc(Pageable pageable);     // 전체
    Optional<Notice> findByIdAndActiveTrue(Long id);                   // 단건(활성)
    Page<Notice> findByActiveTrueOrderByCreatedAtDesc(Pageable pageable); // 활성만
    Page<Notice> findByActiveOrderByCreatedAtDesc(boolean active, Pageable pageable); // ← 이름 수정
}