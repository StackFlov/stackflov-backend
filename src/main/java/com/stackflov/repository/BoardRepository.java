package com.stackflov.repository;

import com.stackflov.domain.Board;
import com.stackflov.domain.User;
import org.springframework.data.domain.Page; // Page 임포트 추가
import org.springframework.data.domain.Pageable; // Pageable 임포트 추가
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {

    // 모든 활성 게시글을 페이지네이션하여 조회
    Page<Board> findByActiveTrue(Pageable pageable);

    // ID로 활성 게시글 단건 조회
    Optional<Board> findByIdAndActiveTrue(Long id);

    // 특정 사용자의 모든 게시글 조회 (활성화/비활성화 상태와 무관)
    List<Board> findByUser(User user);

    // 이전에 있던 findById(Long id)는 findByIdAndActiveTrue로 대체되거나
    // 필요에 따라 유지될 수 있지만, 활성 상태 체크가 기본이 되어야 하므로
    // findByIdAndActiveTrue를 사용하는 것이 권장됩니다.
    // Optional<Board> findById(Long id);
}