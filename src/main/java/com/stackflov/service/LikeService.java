package com.stackflov.service;

import com.stackflov.domain.Board;
import com.stackflov.domain.Like;
import com.stackflov.domain.User;
import com.stackflov.repository.BoardRepository;
import com.stackflov.repository.LikeRepository;
import com.stackflov.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;

    @Transactional
    public void addLike(String email, Long boardId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        Optional<Like> existing = likeRepository.findByUserAndBoard(user, board);

        if (existing.isPresent()) {
            Like like = existing.get();
            if (like.isActive()) {
                throw new IllegalArgumentException("이미 좋아요를 누른 게시글입니다.");
            }
            like.activate();                 // 👈 리액티베이션
            likeRepository.save(like);
            return;
        }

        likeRepository.save(Like.builder().user(user).board(board).build());
    }

    @Transactional
    public void removeLike(String email, Long boardId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        Like like = likeRepository.findByUserAndBoardAndActiveTrue(user, board)
                .orElseThrow(() -> new IllegalArgumentException("좋아요 기록이 없습니다."));
        like.deactivate(); // 소프트 삭제
    }
}
