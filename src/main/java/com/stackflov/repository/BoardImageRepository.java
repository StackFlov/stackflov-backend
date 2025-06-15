package com.stackflov.repository;

import com.stackflov.domain.Board;
import com.stackflov.domain.BoardImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoardImageRepository extends JpaRepository<BoardImage, Long> {
    // ✅ 특정 Board에 속한 모든 BoardImage들을 조회하는 메서드 (BoardService에서 사용)
    List<BoardImage> findByBoard(Board board);

    // (선택적) 특정 Board에 속한 활성화된 BoardImage들만 조회
    // List<BoardImage> findByBoardAndActiveTrue(Board board);
}