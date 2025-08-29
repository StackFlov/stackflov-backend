package com.stackflov.repository;

import com.stackflov.domain.Board;
import com.stackflov.domain.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long>, JpaSpecificationExecutor<Board> {
    // 활성화된 게시글만 ID로 조회
    Optional<Board> findByIdAndActiveTrue(Long id);

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

}