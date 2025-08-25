package com.stackflov.repository;

import com.stackflov.domain.Comment;
import com.stackflov.domain.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    // 특정 게시글에 달린 댓글들 조회
    List<Comment> findByBoardId(Long boardId);

    Optional<Comment> findByIdAndActiveTrue(Long id);

    // 특정 게시글에 달린 활성화된 댓글만 조회
    List<Comment> findByBoardIdAndActiveTrue(Long boardId);

    long countByActiveTrue();

    @Query("SELECT c FROM Comment c WHERE c.content LIKE %:keyword% OR c.user.nickname LIKE %:keyword%")
    Page<Comment> searchAllBy(@Param("keyword") String keyword, Pageable pageable);

    Page<Comment> findByUser(User user, Pageable pageable);

    List<Comment> findByUser(User user);
}
