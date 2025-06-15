package com.stackflov.repository;

import com.stackflov.domain.Board; // Board 임포트 추가
import com.stackflov.domain.Comment;
import com.stackflov.domain.User; // User 임포트 추가
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository; // Repository 임포트 추가

import java.util.List;
import java.util.Optional; // Optional 임포트 추가

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // ✅ 특정 게시글에 달린 활성 상태의 댓글들 조회
    List<Comment> findByBoardAndActiveTrue(Board board);

    // ✅ 특정 게시글에 달린 모든 댓글들 조회 (비활성화 로직에 사용)
    List<Comment> findByBoard(Board board);

    // ✅ 특정 사용자가 작성한 모든 댓글들 조회 (사용자 비활성화 로직에 사용)
    List<Comment> findByUser(User user);

    // ID로 활성 댓글 단건 조회
    Optional<Comment> findByIdAndActiveTrue(Long id);
}