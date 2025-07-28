package com.stackflov.repository;

import com.stackflov.domain.Board;
import com.stackflov.domain.Like;
import com.stackflov.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {

    // 사용자와 게시글로 '좋아요' 기록 찾기
    Optional<Like> findByUserAndBoard(User user, Board board);

    // 특정 게시글의 좋아요 개수 세기 (성능을 위해 count 쿼리 사용)
    long countByBoard(Board board);

    // 특정 사용자가 특정 게시글을 좋아요 했는지 확인
    boolean existsByUserAndBoard(User user, Board board);
}
