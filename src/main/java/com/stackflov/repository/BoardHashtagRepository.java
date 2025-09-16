package com.stackflov.repository;

import com.stackflov.domain.Board;
import com.stackflov.domain.BoardHashtag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardHashtagRepository extends JpaRepository<BoardHashtag, Long> {
    void deleteAllByBoard(Board board);
}