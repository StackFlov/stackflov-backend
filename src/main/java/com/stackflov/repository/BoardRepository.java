package com.stackflov.repository;

import com.stackflov.domain.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long> {
    // 활성화된 게시글만 ID로 조회
    Optional<Board> findByIdAndActiveTrue(Long id);

    // 활성화된 모든 게시글을 페이징하여 조회
    Page<Board> findAllByActiveTrue(Pageable pageable);
}