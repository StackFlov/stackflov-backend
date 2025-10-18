package com.stackflov.service;

import com.stackflov.domain.*;
import com.stackflov.dto.LikeRequestDto;
import com.stackflov.repository.BoardRepository;
import com.stackflov.repository.LikeRepository;
import com.stackflov.repository.ReviewRepository;
import com.stackflov.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final BoardRepository boardRepository;
    private final NotificationService notificationService;
    private final UserService userService;
    private final ReviewRepository reviewRepository;

    @Transactional
    public void addLike(String email, LikeRequestDto req) {
        req.validate();
        User user = userService.getValidUserByEmail(email);

        if (req.isBoardLike()) {
            Board board = boardRepository.findById(req.getBoardId())
                    .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

            Optional<Like> existing = likeRepository.findByUserAndBoard(user, board);
            if (existing.isPresent()) {
                Like like = existing.get();
                if (like.isActive()) throw new IllegalArgumentException("이미 좋아요를 누른 게시글입니다.");
                like.activate();
                // 알림 (본인 글 제외)
                if (!board.getAuthor().getId().equals(user.getId())) {
                    notificationService.notify(
                            board.getAuthor(),
                            NotificationType.LIKE,
                            user.getNickname() + "님이 \"" + board.getTitle() + "\"를 좋아합니다.",
                            "/boards/" + board.getId()
                    );
                }
                return;
            }

            likeRepository.save(Like.builder().user(user).board(board).build());
            if (!board.getAuthor().getId().equals(user.getId())) {
                notificationService.notify(
                        board.getAuthor(),
                        NotificationType.LIKE,
                        user.getNickname() + "님이 \"" + board.getTitle() + "\"를 좋아합니다.",
                        "/boards/" + board.getId()
                );
            }
            return;
        }

        // 리뷰 좋아요
        Review review = reviewRepository.findById(req.getReviewId())
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));

        Optional<Like> existing = likeRepository.findByUserAndReview(user, review);
        if (existing.isPresent()) {
            Like like = existing.get();
            if (like.isActive()) throw new IllegalArgumentException("이미 좋아요를 누른 리뷰입니다.");
            like.activate();
            if (!review.getAuthor().getId().equals(user.getId())) {
                notificationService.notify(
                        review.getAuthor(),
                        NotificationType.LIKE,
                        user.getNickname() + "님이 리뷰 \"" + review.getTitle() + "\"를 좋아합니다.",
                        // TODO: 실제 프론트 라우팅에 맞춰 수정
                        "/map/reviews/" + review.getId()
                );
            }
            return;
        }

        likeRepository.save(Like.builder().user(user).review(review).build());
        if (!review.getAuthor().getId().equals(user.getId())) {
            notificationService.notify(
                    review.getAuthor(),
                    NotificationType.LIKE,
                    user.getNickname() + "님이 리뷰 \"" + review.getTitle() + "\"를 좋아합니다.",
                    "/map/reviews/" + review.getId()
            );
        }
    }

    @Transactional
    public void removeLike(String email, LikeRequestDto req) {
        req.validate();
        User user = userService.getValidUserByEmail(email);

        if (req.isBoardLike()) {
            Board board = boardRepository.findById(req.getBoardId())
                    .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
            Like like = likeRepository.findByUserAndBoardAndActiveTrue(user, board)
                    .orElseThrow(() -> new IllegalArgumentException("좋아요 기록이 없습니다."));
            like.deactivate();
            return;
        }

        Review review = reviewRepository.findById(req.getReviewId())
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));
        Like like = likeRepository.findByUserAndReviewAndActiveTrue(user, review)
                .orElseThrow(() -> new IllegalArgumentException("좋아요 기록이 없습니다."));
        like.deactivate();
    }
}
