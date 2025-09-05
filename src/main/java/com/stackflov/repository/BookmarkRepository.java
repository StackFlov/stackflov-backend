package com.stackflov.repository;

import com.stackflov.domain.Board;
import com.stackflov.domain.Bookmark;
import com.stackflov.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    Optional<Bookmark> findByUserAndBoard(User user, Board board);

    List<Bookmark> findByUser(User user);

    List<Bookmark> findByBoard(Board board);

    void deleteByUserAndBoard(User user, Board board);

    // ⬇ 소프트 삭제 대응 (활성 기준)
    Optional<Bookmark> findByUserAndBoardAndActiveTrue(User user, Board board);
    List<Bookmark> findByUserAndActiveTrue(User user);
}
