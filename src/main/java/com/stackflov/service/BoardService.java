package com.stackflov.service;

import com.stackflov.domain.*;
import com.stackflov.dto.*;
import com.stackflov.repository.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final BoardImageRepository boardImageRepository;
    private final UserRepository userRepository;
    private final BookmarkRepository bookmarkRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public Long createBoard(String email, BoardRequestDto dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Board board = Board.builder()
                .author(user)
                .title(dto.getTitle())
                .content(dto.getContent())
                .category(dto.getCategory())
                .build();

        // 이미지 처리
        if (dto.getImageUrls() != null && !dto.getImageUrls().isEmpty()) {
            List<BoardImage> images = dto.getImageUrls().stream()
                    .map(url -> BoardImage.builder().board(board).imageUrl(url).build())
                    .collect(Collectors.toList());
            board.getImages().addAll(images);
        }

        Board savedBoard = boardRepository.save(board);
        return savedBoard.getId();
    }


    @Transactional
    public BoardResponseDto getBoard(Long boardId, String email) {
        Board board = boardRepository.findByIdAndActiveTrue(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없거나 삭제되었습니다."));

        board.increaseViewCount(); // 조회수 증가

        boolean isLiked = userRepository.findByEmail(email)
                .map(user -> likeRepository.existsByUserAndBoardAndActiveTrue(user, board))
                .orElse(false);

        List<String> imageUrls = board.getImages().stream()
                .map(BoardImage::getImageUrl)
                .collect(Collectors.toList());

        // 👇 Board 엔티티를 BoardResponseDto로 변환합니다.
        return BoardResponseDto.builder()
                .id(board.getId())
                .title(board.getTitle())
                .content(board.getContent())
                .category(board.getCategory())
                .authorEmail(board.getAuthor().getEmail())
                .authorNickname(board.getAuthor().getNickname())
                .authorId(board.getAuthor().getId())
                .imageUrls(imageUrls)
                .viewCount(board.getViewCount())
                .createdAt(board.getCreatedAt())
                .updatedAt(board.getUpdatedAt())
                .likeCount(likeRepository.countByBoardAndActiveTrue(board))
                .isLiked(isLiked)
                .build();
    }

    @Transactional(readOnly = true)
    public Page<BoardListResponseDto> getBoards(int page, int size, String userEmail) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Board> boards = boardRepository.findAllByActiveTrue(pageable);

        // 사용자가 로그인한 경우, 북마크한 게시글 ID 목록을 미리 준비합니다.
        Set<Long> bookmarkedBoardIds = new HashSet<>();
        User currentUser = null;
        if (userEmail != null) {
            currentUser = userRepository.findByEmail(userEmail).orElse(null);
            if (currentUser != null) {
                List<Bookmark> bookmarks = bookmarkRepository.findByUserAndActiveTrue(currentUser);
                bookmarks.forEach(b -> bookmarkedBoardIds.add(b.getBoard().getId()));
            }
        }

        final User finalCurrentUser = currentUser; // 람다식 내부에서 사용하기 위해 final 변수로 선언

        // 👇 Board 엔티티를 BoardListResponseDto로 변환합니다.
        return boards.map(board -> BoardListResponseDto.builder()
                .id(board.getId())
                .title(board.getTitle())
                .authorEmail(board.getAuthor().getEmail())
                .authorNickname(board.getAuthor().getNickname())
                .authorId(board.getAuthor().getId())
                .category(board.getCategory())
                .thumbnailUrl(board.getImages().isEmpty() ? null : board.getImages().get(0).getImageUrl())
                .viewCount(board.getViewCount())
                .createdAt(board.getCreatedAt())
                .updatedAt(board.getUpdatedAt())
                .likeCount(likeRepository.countByBoardAndActiveTrue(board))
                .isBookmarked(bookmarkedBoardIds.contains(board.getId())) // 미리 준비한 Set으로 북마크 여부 확인
                .isLiked(finalCurrentUser != null && likeRepository.existsByUserAndBoardAndActiveTrue(finalCurrentUser, board)) // 로그인한 경우 좋아요 여부 확인
                .build());
    }

    @Transactional
    public void updateBoard(String email, Long boardId, BoardUpdateRequestDto dto) {
        Board board = boardRepository.findByIdAndActiveTrue(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없거나 삭제되었습니다."));

        if (!board.getAuthor().getEmail().equals(email)) {
            throw new IllegalArgumentException("작성자만 수정할 수 있습니다.");
        }

        board.update(dto.getTitle(), dto.getContent(), dto.getCategory());

        // 이미지 업데이트 로직 (기존 이미지 모두 삭제 후 새로 추가)
        board.getImages().clear();
        if (dto.getImageUrls() != null && !dto.getImageUrls().isEmpty()) {
            List<BoardImage> newImages = dto.getImageUrls().stream()
                    .map(url -> BoardImage.builder().board(board).imageUrl(url).build())
                    .collect(Collectors.toList());
            board.getImages().addAll(newImages);
        }
    }


    @Transactional
    public void deleteBoard(String email, Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        if (!board.getAuthor().getEmail().equals(email)) {
            throw new IllegalArgumentException("작성자만 삭제할 수 있습니다.");
        }

        boardRepository.delete(board);
    }

    @Transactional
    public void deactivateOwnBoard(String email, Long boardId) {
        Board board = boardRepository.findByIdAndActiveTrue(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없거나 이미 삭제되었습니다."));

        if (!board.getAuthor().getEmail().equals(email)) {
            throw new IllegalArgumentException("작성자만 삭제할 수 있습니다.");
        }
        board.deactivate(); // active를 false로 변경

        List<Comment> comments = commentRepository.findByBoardId(boardId);
        for (Comment comment : comments) {
            comment.deactivate();
        }
        List<Bookmark> bookmarks = bookmarkRepository.findByBoard(board);
        for (Bookmark bookmark : bookmarks) {
            bookmark.deactivate();
        }

        List<Like> likes = likeRepository.findByBoard(board);
        for (Like like : likes) {
            like.deactivate();
        }
    }

    @Transactional
    public void deactivateBoardByAdmin(Long boardId) {
        Board board = boardRepository.findById(boardId) // 관리자는 비활성화된 글도 찾을 수 있어야 하므로 findById 사용
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        board.deactivate(); // active를 false로 변경
    }

    private void deactivateBoardAndAssociations(Board board) {
        // 1. 게시글 비활성화
        board.deactivate();

        // 2. 연관된 북마크들 비활성화
        List<Bookmark> bookmarks = bookmarkRepository.findByBoard(board);
        bookmarks.forEach(Bookmark::deactivate);

        // 3. 연관된 좋아요들 비활성화
        List<Like> likes = likeRepository.findByBoard(board);
        likes.forEach(Like::deactivate);
    }
    // [추가] 특정 사용자의 모든 게시글을 비활성화
    @Transactional
    public void deactivateAllBoardsByUser(User user) {
        List<Board> boards = boardRepository.findByAuthor(user);
        for (Board board : boards) {
            // 이전에 만든, 연관 데이터까지 함께 비활성화하는 메서드를 재사용
            deactivateBoardAndAssociations(board);
        }
    }
    @Transactional(readOnly = true)
    public Page<BoardResponseDto> searchBoards(BoardSearchConditionDto condition, Pageable pageable) {
        Specification<Board> spec = BoardSpecification.search(condition);
        Page<Board> boards = boardRepository.findAll(spec, pageable);
        return boards.map(BoardResponseDto::new);
    }
}