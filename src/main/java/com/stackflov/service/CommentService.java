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
    public CommentResponseDto createComment(CommentRequestDto commentRequestDto, String userEmail) { // 반환 타입 Long -> CommentResponseDto
        // 활성 사용자만 댓글 작성 가능
        User user = userRepository.findByEmailAndActiveTrue(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("활성화된 사용자만 댓글을 작성할 수 있습니다."));

        // 활성 게시글에만 댓글 작성 가능
        Board board = boardRepository.findByIdAndActiveTrue(commentRequestDto.getBoardId())
                .orElseThrow(() -> new IllegalArgumentException("활성화된 게시글에만 댓글을 작성할 수 있습니다."));

        Comment comment = Comment.builder()
                .board(board)
                .user(user)
                .title(commentRequestDto.getTitle())
                .content(commentRequestDto.getContent())
                .build(); // active는 기본값 true로 생성됨

        commentRepository.save(comment);
        return new CommentResponseDto(comment); // 생성된 댓글 DTO 반환
    }

    // ✅ 특정 게시글의 활성 댓글 목록 조회 (기존 findByBoardId 대체)
    @Transactional(readOnly = true)
    public List<CommentResponseDto> getActiveCommentsByBoard(Long boardId) {
        Board board = boardRepository.findByIdAndActiveTrue(boardId)
                .orElseThrow(() -> new IllegalArgumentException("활성화된 게시글을 찾을 수 없습니다."));

        List<Comment> comments = commentRepository.findByBoardAndActiveTrue(board);
        return comments.stream()
                .map(CommentResponseDto::new)
                .collect(Collectors.toList());
    }

    // 댓글 수정
    @Transactional
    public CommentResponseDto updateComment(Long commentId, String title, String content, String userEmail) { // 반환 타입 CommentResponseDto
        // 활성 사용자만 댓글 수정 가능
        User user = userRepository.findByEmailAndActiveTrue(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("활성화된 사용자만 댓글을 수정할 수 있습니다."));

        Comment comment = commentRepository.findByIdAndActiveTrue(commentId) // 활성 댓글만 수정 가능
                .orElseThrow(() -> new IllegalArgumentException("활성화된 댓글을 찾을 수 없습니다."));

        // 댓글 작성자 확인
        if (!comment.getUser().getEmail().equals(userEmail)) {
            throw new IllegalArgumentException("자신이 작성한 댓글만 수정할 수 있습니다.");
        }

        comment.updateTitle(title);
        comment.updateContent(content);
        return new CommentResponseDto(comment); // 수정된 댓글 DTO 반환
    }

    // 댓글 삭제 (실제 삭제 대신 비활성화)
    @Transactional
    public void deleteComment(Long commentId, String userEmail) {
        // 활성 사용자만 댓글 삭제(비활성화) 가능
        User user = userRepository.findByEmailAndActiveTrue(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("활성화된 사용자만 댓글을 삭제할 수 있습니다."));

        Comment comment = commentRepository.findByIdAndActiveTrue(commentId) // 활성 댓글만 비활성화 가능
                .orElseThrow(() -> new IllegalArgumentException("활성화된 댓글을 찾을 수 없습니다."));

        // 댓글 작성자 확인
        if (!comment.getUser().getEmail().equals(userEmail)) {
            throw new IllegalArgumentException("자신이 작성한 댓글만 삭제할 수 있습니다.");
        }

        comment.deactivate(); // active 상태를 FALSE로 변경
        // commentRepository.save(comment); // @Transactional이 있으므로 변경 감지 후 자동 저장
    }

    // ✅ 특정 사용자의 모든 댓글을 비활성화하는 메서드 (UserService에서 호출)
    @Transactional
    public void deactivateCommentsByUser(User user) {
        List<Comment> userComments = commentRepository.findByUser(user);
        for (Comment comment : userComments) {
            comment.deactivate();
        }
        commentRepository.saveAll(userComments); // 변경된 상태 저장
    }

    // ✅ 특정 사용자의 모든 댓글을 활성화하는 메서드 (UserService에서 호출)
    @Transactional
    public void activateCommentsByUser(User user) {
        List<Comment> userComments = commentRepository.findByUser(user);
        for (Comment comment : userComments) {
            comment.activate();
        }
        commentRepository.saveAll(userComments); // 변경된 상태 저장
    }

    // ✅ 특정 게시글의 모든 댓글을 비활성화하는 메서드 (BoardService에서 호출)
    @Transactional
    public void deactivateCommentsByBoard(Board board) {
        List<Comment> boardComments = commentRepository.findByBoard(board);
        for (Comment comment : boardComments) {
            comment.deactivate();
        }
        commentRepository.saveAll(boardComments); // 변경된 상태 저장
    }

    // ✅ 특정 게시글의 모든 댓글을 활성화하는 메서드 (BoardService에서 호출)
    @Transactional
    public void activateCommentsByBoard(Board board) {
        List<Comment> boardComments = commentRepository.findByBoard(board);
        for (Comment comment : boardComments) {
            comment.activate();
        }
        commentRepository.saveAll(boardComments); // 변경된 상태 저장
    }
}