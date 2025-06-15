package com.stackflov.service;

import com.stackflov.domain.Board;
import com.stackflov.domain.BoardImage;
import com.stackflov.domain.User;
import com.stackflov.dto.BoardRequestDto;
import com.stackflov.dto.BoardResponseDto;
import com.stackflov.repository.BoardImageRepository;
import com.stackflov.repository.BoardRepository;
import com.stackflov.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final BoardImageRepository boardImageRepository;
    private final CommentService commentService; // ✅ CommentService 주입

    // 게시글 생성
    @Transactional
    public BoardResponseDto createBoard(String userEmail, BoardRequestDto requestDto) {
        User user = userRepository.findByEmailAndActiveTrue(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("활성화된 사용자만 게시글을 작성할 수 있습니다."));

        Board board = Board.builder()
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .category(requestDto.getCategory())
                .viewCount(0)
                .user(user)
                .build();

        Board savedBoard = boardRepository.save(board);

        if (requestDto.getImageUrls() != null && !requestDto.getImageUrls().isEmpty()) {
            for (String imageUrl : requestDto.getImageUrls()) {
                BoardImage boardImage = BoardImage.builder()
                        .board(savedBoard)
                        .imageUrl(imageUrl)
                        .build();
                savedBoard.addBoardImage(boardImage);
            }
            boardImageRepository.saveAll(savedBoard.getBoardImages());
        }

        return new BoardResponseDto(savedBoard);
    }

    // 게시글 상세 조회 (조회수 증가 로직 포함)
    @Transactional
    public BoardResponseDto getBoard(Long boardId) {
        Board board = boardRepository.findByIdAndActiveTrue(boardId)
                .orElseThrow(() -> new IllegalArgumentException("활성화된 게시글을 찾을 수 없습니다."));

        board.incrementViewCount();
        return new BoardResponseDto(board);
    }

    // 게시글 목록 조회 (활성 상태인 게시글만 조회)
    @Transactional(readOnly = true)
    public Page<BoardResponseDto> getBoards(Pageable pageable) {
        Page<Board> boards = boardRepository.findByActiveTrue(pageable);
        return boards.map(BoardResponseDto::new);
    }

    // 게시글 수정
    @Transactional
    public BoardResponseDto updateBoard(Long boardId, String userEmail, BoardRequestDto requestDto) {
        User user = userRepository.findByEmailAndActiveTrue(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("활성화된 사용자만 게시글을 수정할 수 있습니다."));

        Board board = boardRepository.findByIdAndActiveTrue(boardId)
                .orElseThrow(() -> new IllegalArgumentException("활성화된 게시글을 찾을 수 없습니다."));

        if (!board.getUser().getEmail().equals(userEmail)) {
            throw new IllegalArgumentException("자신이 작성한 게시글만 수정할 수 있습니다.");
        }

        board.update(requestDto.getTitle(), requestDto.getContent(), requestDto.getCategory());

        boardImageRepository.deleteAll(board.getBoardImages());
        board.getBoardImages().clear();

        if (requestDto.getImageUrls() != null && !requestDto.getImageUrls().isEmpty()) {
            for (String imageUrl : requestDto.getImageUrls()) {
                BoardImage boardImage = BoardImage.builder()
                        .board(board)
                        .imageUrl(imageUrl)
                        .build();
                board.addBoardImage(boardImage);
            }
            boardImageRepository.saveAll(board.getBoardImages());
        }

        return new BoardResponseDto(board);
    }

    // 게시글 삭제 (실제 삭제 대신 비활성화)
    @Transactional
    public void deleteBoard(Long boardId, String userEmail) {
        User user = userRepository.findByEmailAndActiveTrue(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("활성화된 사용자만 게시글을 삭제할 수 있습니다."));

        Board board = boardRepository.findByIdAndActiveTrue(boardId)
                .orElseThrow(() -> new IllegalArgumentException("활성화된 게시글을 찾을 수 없습니다."));

        if (!board.getUser().getEmail().equals(userEmail)) {
            throw new IllegalArgumentException("자신이 작성한 게시글만 삭제할 수 있습니다.");
        }

        board.deactivate();

        List<BoardImage> boardImages = boardImageRepository.findByBoard(board);
        for (BoardImage image : boardImages) {
            image.deactivate();
        }
        boardImageRepository.saveAll(boardImages);

        // ✅ 게시글에 연관된 모든 댓글들도 비활성화
        commentService.deactivateCommentsByBoard(board);
    }

    // 특정 사용자의 모든 게시글을 비활성화하는 메서드 (UserService에서 호출)
    @Transactional
    public void deactivateBoardsByUser(User user) {
        List<Board> userBoards = boardRepository.findByUser(user);
        for (Board board : userBoards) {
            board.deactivate();

            List<BoardImage> boardImages = boardImageRepository.findByBoard(board);
            for (BoardImage image : boardImages) {
                image.deactivate();
            }
            boardImageRepository.saveAll(boardImages);

            // ✅ 게시글에 연관된 모든 댓글들도 비활성화
            commentService.deactivateCommentsByBoard(board);
        }
    }

    // 특정 사용자의 모든 게시글을 활성화하는 메서드 (UserService에서 호출)
    @Transactional
    public void activateBoardsByUser(User user) {
        List<Board> userBoards = boardRepository.findByUser(user);
        for (Board board : userBoards) {
            board.activate();

            List<BoardImage> boardImages = boardImageRepository.findByBoard(board);
            for (BoardImage image : boardImages) {
                image.activate();
            }
            boardImageRepository.saveAll(boardImages);

            // ✅ 게시글에 연관된 모든 댓글들도 활성화
            commentService.activateCommentsByBoard(board);
        }
    }
}