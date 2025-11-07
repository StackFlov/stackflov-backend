package com.stackflov.repository;

import com.stackflov.domain.Board;
import com.stackflov.domain.BoardHashtag;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BoardHashtagRepository extends JpaRepository<BoardHashtag, Long> {
    void deleteAllByBoard(Board board);
    @Query("""
           select distinct h.name
           from BoardHashtag bh
           join bh.hashtag h
           where bh.board.id = :boardId
           order by lower(h.name)
           """)
    List<String> findHashtagNamesByBoardId(@Param("boardId") Long boardId);
}