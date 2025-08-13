package com.stackflov.repository;

import com.stackflov.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    // 특정 게시글에 달린 댓글들 조회
    List<Comment> findByBoardId(Long boardId);

    Optional<Comment> findByIdAndActiveTrue(Long id);

    // 특정 게시글에 달린 활성화된 댓글만 조회
    List<Comment> findByBoardIdAndActiveTrue(Long boardId);

    long countByActiveTrue();
}
