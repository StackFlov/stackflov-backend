package com.stackflov.service;

import com.stackflov.domain.Board;
import com.stackflov.domain.Comment;
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

    // 댓글 생성
    @Transactional
    public Long createComment(CommentRequestDto commentRequestDto, String userEmail) {

        // 사용자 이메일을 이용하여 유저를 조회
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("유저가 존재하지 않습니다."));

        // 게시글 ID로 게시글을 조회
        Board board = boardRepository.findById(commentRequestDto.getBoardId())
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        // 댓글 생성
        Comment comment = Comment.builder()
                .board(board)
                .user(user)
                .content(commentRequestDto.getContent())  // content 설정
                .build();

        commentRepository.save(comment);
        return comment.getId();  // 생성된 댓글 ID 반환
    }

    // 댓글 수정
    @Transactional
    public void updateComment(Long commentId, String content, String userEmail) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다."));

        // 댓글 작성자가 아니라면 수정할 수 없음
        if (!comment.getUser().getEmail().equals(userEmail)) {
            throw new IllegalArgumentException("작성자만 수정할 수 있습니다.");
        }

        // 댓글 내용 수정
        comment.updateContent(content);  // 내용 수정
    }
    // 댓글 삭제
    @Transactional
    public void deleteComment(Long commentId, String userEmail) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다."));

        // 댓글 작성자가 아니라면 삭제할 수 없음
        if (!comment.getUser().getEmail().equals(userEmail)) {
            throw new IllegalArgumentException("작성자만 삭제할 수 있습니다.");
        }

        commentRepository.delete(comment);  // 댓글 삭제
    }
    // 게시글에 달린 모든 댓글 조회
    @Transactional(readOnly = true)
    public List<CommentResponseDto> getCommentsByBoardId(Long boardId) {
        List<Comment> comments = commentRepository.findByBoardId(boardId);
        return comments.stream()
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

