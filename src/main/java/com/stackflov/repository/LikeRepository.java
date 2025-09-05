package com.stackflov.repository;

import com.stackflov.domain.Board;
import com.stackflov.domain.Like;
import com.stackflov.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {

    Optional<Like> findByUserAndBoard(User user, Board board);

    long countByBoard(Board board);

    boolean existsByUserAndBoard(User user, Board board);

    // ⬇ 소프트 삭제 대응 (활성 기준)
    Optional<Like> findByUserAndBoardAndActiveTrue(User user, Board board);
    long countByBoardAndActiveTrue(Board board);
    boolean existsByUserAndBoardAndActiveTrue(User user, Board board);

    List<Like> findByBoard(Board board);
}
