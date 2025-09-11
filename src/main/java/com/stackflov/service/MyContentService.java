package com.stackflov.service;

import com.stackflov.domain.User;
import com.stackflov.dto.*;
import com.stackflov.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyContentService {

    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;
    private final ReviewRepository reviewRepository;

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    public Page<BoardListResponseDto> getMyBoards(String email, Pageable pageable) {
        User user = getUserByEmail(email);
        return boardRepository.findByAuthor(user, pageable).map(BoardListResponseDto::new);
    }

    public Page<ReviewResponseDto> getMyReviews(String email, Pageable pageable) {
        User user = getUserByEmail(email);
        return reviewRepository.findByAuthor(user, pageable).map(ReviewResponseDto::new);
    }

    public Page<CommentResponseDto> getMyBoardComments(String email, Pageable pageable) {
        User user = getUserByEmail(email);
        return commentRepository.findByUserAndBoardIsNotNull(user, pageable)
                .map(c -> new CommentResponseDto(c.getId(), c.getContent(), c.getUser().getEmail(), c.getCreatedAt(), c.getUpdatedAt()));
    }

    public Page<CommentResponseDto> getMyReviewComments(String email, Pageable pageable) {
        User user = getUserByEmail(email);
        return commentRepository.findByUserAndReviewIsNotNull(user, pageable)
                .map(c -> new CommentResponseDto(c.getId(), c.getContent(), c.getUser().getEmail(), c.getCreatedAt(), c.getUpdatedAt()));
    }
}