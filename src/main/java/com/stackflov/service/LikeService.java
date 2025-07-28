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

        // 이미 좋아요를 눌렀는지 확인
        if (likeRepository.findByUserAndBoard(user, board).isPresent()) {
            throw new IllegalArgumentException("이미 좋아요를 누른 게시글입니다.");
        }

        Like like = Like.builder()
                .user(user)
                .board(board)
                .build();

        likeRepository.save(like);
    }

    @Transactional
    public void removeLike(String email, Long boardId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        Like like = likeRepository.findByUserAndBoard(user, board)
                .orElseThrow(() -> new IllegalArgumentException("좋아요 기록을 찾을 수 없습니다."));

        likeRepository.delete(like);
    }
}

