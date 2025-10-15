package com.stackflov.service;

import com.stackflov.domain.*;
import com.stackflov.dto.CommentRequestDto;
import com.stackflov.dto.CommentResponseDto;
import com.stackflov.repository.BoardRepository;
import com.stackflov.repository.CommentRepository;
import com.stackflov.repository.ReviewRepository;
import com.stackflov.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final NotificationService notificationService;
    private final ReviewRepository reviewRepository;
    private final UserService userService;
    private final BannedWordService bannedWordService;
    private final MentionService mentionService;

    @Transactional
    public Long createComment(CommentRequestDto dto, String userEmail) {
        User user = userService.getValidUserByEmail(userEmail);

        if (bannedWordService.containsBannedWord(dto.getContent())) {
            throw new IllegalArgumentException("내용에 금지된 단어가 포함되어 있습니다.");
        }

        // 둘 다/둘 다 아님 방지
        if ((dto.getBoardId() == null && dto.getReviewId() == null) ||
                (dto.getBoardId() != null && dto.getReviewId() != null)) {
            throw new IllegalArgumentException("게시글 또는 리뷰 중 하나의 ID만 전달하세요.");
        }

        Comment.CommentBuilder commentBuilder = Comment.builder()
                .user(user)
                .content(dto.getContent());

        if (dto.getBoardId() != null) {
            // 게시글 댓글
            Board board = boardRepository.findById(dto.getBoardId())
                    .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));
            commentBuilder.board(board);

            if (!board.getAuthor().getId().equals(user.getId())) {
                notificationService.notify(
                        board.getAuthor(),
                        NotificationType.COMMENT,
                        user.getNickname() + "님이 회원님의 글에 댓글을 남겼습니다.",
                        "/boards/" + board.getId()          // ✅ 그대로
                );
            }

        } else {
            // 리뷰 댓글
            Review review = reviewRepository.findByIdAndActiveTrue(dto.getReviewId())
                    .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));
            commentBuilder.review(review);

            if (!review.getAuthor().getId().equals(user.getId())) {
                notificationService.notify(
                        review.getAuthor(),
                        NotificationType.COMMENT,             // 원하면 REVIEW_COMMENT로 분리
                        user.getNickname() + "님이 회원님의 리뷰에 댓글을 남겼습니다.",
                        "/reviews/" + review.getId()          // ✅ Location 제거에 맞게 수정
                        //  예: 프론트 라우트가 /map/reviews/:id 라면 여기를 맞춰주세요.
                );
            }
        }

        Comment comment = commentRepository.save(commentBuilder.build());

        // mentionService 시그니처가 (board, review) 중 하나 null 허용인지 확인
        mentionService.processMentions(user, dto.getContent(), comment.getBoard(), comment);
        return comment.getId();
    }


    @Transactional
    public void updateComment(Long commentId, String content, String userEmail) {
        Comment comment = commentRepository.findByIdAndActiveTrue(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없거나 삭제되었습니다."));

        if (!comment.getUser().getEmail().equals(userEmail)) {
            throw new IllegalArgumentException("작성자만 수정할 수 있습니다.");
        }

        if (bannedWordService.containsBannedWord(content)) {
            throw new IllegalArgumentException("내용에 금지된 단어가 포함되어 있습니다.");
        }

        comment.updateContent(content);
        mentionService.processMentions(comment.getUser(), content, comment.getBoard(), comment);
    }

    // ⬇ 소프트 삭제
    @Transactional
    public void deleteComment(Long commentId, String userEmail) {
        Comment comment = commentRepository.findByIdAndActiveTrue(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없거나 삭제되었습니다."));

        if (!comment.getUser().getEmail().equals(userEmail)) {
            throw new IllegalArgumentException("작성자만 삭제할 수 있습니다.");
        }
        comment.deactivate();
    }

    @Transactional(readOnly = true)
    public List<CommentResponseDto> getCommentsByBoardId(Long boardId) {
        return commentRepository.findByBoardIdAndActiveTrue(boardId).stream()
                .map(comment -> new CommentResponseDto(
                        comment.getId(),
                        comment.getContent(),
                        comment.getUser().getEmail(),
                        comment.getCreatedAt(),
                        comment.getUpdatedAt()
                ))
                .collect(Collectors.toList());
    }

    // 관리자도 소프트 삭제
    @Transactional
    public void deleteCommentByAdmin(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 댓글이 존재하지 않습니다."));
        comment.deactivate();
    }

    @Transactional
    public void deactivateOwnComment(Long commentId, String userEmail) {
        Comment comment = commentRepository.findByIdAndActiveTrue(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없거나 이미 삭제되었습니다."));

        if (!comment.getUser().getEmail().equals(userEmail)) {
            throw new IllegalArgumentException("작성자만 삭제할 수 있습니다.");
        }
        comment.deactivate();
    }

    @Transactional
    public void deactivateCommentByAdmin(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
        comment.deactivate();
    }

    @Transactional
    public void deactivateAllCommentsByUser(User user) {
        commentRepository.findByUser(user).forEach(Comment::deactivate);
    }

    @Transactional(readOnly = true)
    public List<CommentResponseDto> getCommentsByReviewId(Long reviewId) {
        // 리뷰가 존재하는지 먼저 확인 (선택 사항이지만 안전함)
        reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));

        return commentRepository.findByReviewIdAndActiveTrue(reviewId).stream()
                .map(comment -> new CommentResponseDto(
                        comment.getId(),
                        comment.getContent(),
                        comment.getUser().getEmail(),
                        comment.getCreatedAt(),
                        comment.getUpdatedAt()
                ))
                .collect(Collectors.toList());
    }
}
