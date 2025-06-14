package com.stackflov.repository;

import com.stackflov.domain.Board;
import com.stackflov.domain.Bookmark;
import com.stackflov.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    // 특정 사용자가 특정 게시글을 북마크했는지 확인 (중복 방지 로직에 활용)
    Optional<Bookmark> findByUserAndBoard(User user, Board board);

    // 특정 사용자의 모든 북마크 조회
    List<Bookmark> findByUser(User user);

    // 특정 게시글의 모든 북마크 조회
    List<Bookmark> findByBoard(Board board);

    // 특정 사용자가 특정 게시글을 북마크한 기록 삭제
    void deleteByUserAndBoard(User user, Board board);
}