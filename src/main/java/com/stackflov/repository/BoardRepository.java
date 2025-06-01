package com.stackflov.repository;

import com.stackflov.domain.Board;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board, Long> {
    // 필요 시 커스텀 메서드 추가
}