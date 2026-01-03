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
        // ⬇️ active=true만
        return boardRepository.findByAuthorAndActiveTrue(user, pageable)
                .map(BoardListResponseDto::new);
    }

    public Page<ReviewResponseDto> getMyReviews(String email, Pageable pageable) {
        User user = getUserByEmail(email);
        // ⬇️ active=true만
        return reviewRepository.findByAuthorAndActiveTrue(user, pageable)
                .map(ReviewResponseDto::new);
    }

    public Page<CommentResponseDto> getMyBoardComments(String email, Pageable pageable) {
        User user = getUserByEmail(email);
        // ⬇️ 내 댓글 + 댓글 active=true + 대상(Board) active=true
        return commentRepository.findActiveByUserOnBoards(user, pageable)
                .map(c -> new CommentResponseDto(c.getId(), c.getContent(), c.getUser().getId(),        // ✅ 추가: authorId
                        c.getUser().getNickname(), c.getUser().getEmail(), c.getCreatedAt(), c.getUpdatedAt()));
    }

    public Page<CommentResponseDto> getMyReviewComments(String email, Pageable pageable) {
        User user = getUserByEmail(email);
        // ⬇️ 내 댓글 + 댓글 active=true + 대상(Review) active=true
        return commentRepository.findActiveByUserOnReviews(user, pageable)
                .map(c -> new CommentResponseDto(c.getId(), c.getContent(), c.getUser().getId(),        // ✅ 추가: authorId
                        c.getUser().getNickname(), c.getUser().getEmail(), c.getCreatedAt(), c.getUpdatedAt()));
    }
}