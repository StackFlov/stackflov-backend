package com.stackflov.repository;

import com.stackflov.domain.Board;
import com.stackflov.domain.User;
import com.stackflov.repository.projection.DailyStatProjection;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long>, JpaSpecificationExecutor<Board> {
    // 활성화된 게시글만 ID로 조회
    Optional<Board> findByIdAndActiveTrue(Long id);
    long countByAuthor_Id(Long authorId);

    // 활성화된 모든 게시글을 페이징하여 조회
    Page<Board> findAllByActiveTrue(Pageable pageable);
    long countByActiveTrue();
    @Query("SELECT b FROM Board b WHERE " +
            "(:type = 'title' AND b.title LIKE %:keyword%) OR " +
            "(:type = 'content' AND b.content LIKE %:keyword%) OR " +
            "(:type = 'author' AND b.author.nickname LIKE %:keyword%)")
    Page<Board> searchAllBy(@Param("type") String type, @Param("keyword") String keyword, Pageable pageable);

    Page<Board> findByAuthor(User author, Pageable pageable);

    Page<Board> findByAuthorAndActiveTrue(User author, Pageable pageable);

    List<Board> findByAuthor(User author);

    Page<Board> findByAuthorInOrderByCreatedAtDesc(List<User> authors, Pageable pageable);

    @Query("SELECT FUNCTION('DATE', b.createdAt) as date, COUNT(b.id) as count FROM Board b WHERE b.createdAt >= :startDate GROUP BY date ORDER BY date")
    List<DailyStatProjection> countDailyBoards(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT bh.board FROM BoardHashtag bh JOIN bh.hashtag h WHERE h.name = :tagName AND bh.board.active = true")
    Page<Board> findByHashtagName(@Param("tagName") String tagName, Pageable pageable);

}