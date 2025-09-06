package com.stackflov.service;

import com.stackflov.domain.Board;
import com.stackflov.domain.Comment;
import com.stackflov.domain.NotificationType;
import com.stackflov.domain.User;
import com.stackflov.dto.CommentRequestDto;
import com.stackflov.dto.CommentResponseDto;
import com.stackflov.repository.BoardRepository;
import com.stackflov.repository.CommentRepository;
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

    @Transactional
    public Long createComment(CommentRequestDto commentRequestDto, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("유저가 존재하지 않습니다."));
        Board board = boardRepository.findById(commentRequestDto.getBoardId())
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        Comment comment = Comment.builder()
                .board(board)
                .user(user)
                .content(commentRequestDto.getContent())
                .build();

        if (!board.getAuthor().getId().equals(user.getId())) {
            notificationService.notify(
                    board.getAuthor(),
                    NotificationType.COMMENT,
                    user.getNickname() + "님이 \"" + board.getTitle() + "\"에 댓글을 남겼습니다.",
                    "/boards/" + board.getId()
            );
        }

        commentRepository.save(comment);
        return comment.getId();
    }

    @Transactional
    public void updateComment(Long commentId, String content, String userEmail) {
        Comment comment = commentRepository.findByIdAndActiveTrue(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없거나 삭제되었습니다."));

        if (!comment.getUser().getEmail().equals(userEmail)) {
            throw new IllegalArgumentException("작성자만 수정할 수 있습니다.");
        }
        comment.updateContent(content);
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
}
