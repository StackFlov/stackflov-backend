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
import java.util.concurrent.atomic.AtomicInteger;
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

        // 이미지 처리 (모두 활성으로 추가)
        if (dto.getImageUrls() != null && !dto.getImageUrls().isEmpty()) {
            AtomicInteger order = new AtomicInteger(0);

            List<BoardImage> images = dto.getImageUrls().stream()
                    .map(url -> BoardImage.builder()
                            .board(board)
                            .imageUrl(url)
                            .sortOrder(order.getAndIncrement())
                            .build())
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

        // 활성 이미지만, sortOrder 기준
        List<String> imageUrls = board.getImages().stream()
                .filter(BoardImage::isActive)
                .sorted(Comparator.comparing(i -> i.getSortOrder() == null ? Integer.MAX_VALUE : i.getSortOrder()))
                .map(BoardImage::getImageUrl)
                .collect(Collectors.toList());

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

        Set<Long> bookmarkedBoardIds = new HashSet<>();
        User currentUser = null;
        if (userEmail != null) {
            currentUser = userRepository.findByEmail(userEmail).orElse(null);
            if (currentUser != null) {
                List<Bookmark> bookmarks = bookmarkRepository.findByUserAndActiveTrue(currentUser);
                bookmarks.forEach(b -> bookmarkedBoardIds.add(b.getBoard().getId()));
            }
        }

        final User finalCurrentUser = currentUser;

        return boards.map(board -> BoardListResponseDto.builder()
                .id(board.getId())
                .title(board.getTitle())
                .authorEmail(board.getAuthor().getEmail())
                .authorNickname(board.getAuthor().getNickname())
                .authorId(board.getAuthor().getId())
                .category(board.getCategory())
                .thumbnailUrl(board.getImages().stream()
                        .filter(BoardImage::isActive)
                        .sorted(Comparator.comparing(i -> i.getSortOrder() == null ? Integer.MAX_VALUE : i.getSortOrder()))
                        .map(BoardImage::getImageUrl)
                        .findFirst().orElse(null))
                .viewCount(board.getViewCount())
                .createdAt(board.getCreatedAt())
                .updatedAt(board.getUpdatedAt())
                .likeCount(likeRepository.countByBoardAndActiveTrue(board))
                .isBookmarked(bookmarkedBoardIds.contains(board.getId()))
                .isLiked(finalCurrentUser != null && likeRepository.existsByUserAndBoardAndActiveTrue(finalCurrentUser, board))
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

        // === 이미지 소프트 삭제/재정렬 ===
        // 현재 활성 이미지 Map (url -> entity)
        Map<String, BoardImage> activeMap = board.getImages().stream()
                .filter(BoardImage::isActive)
                .collect(Collectors.toMap(BoardImage::getImageUrl, img -> img, (a, b) -> a));

        List<String> newUrls = dto.getImageUrls() == null ? List.of() : dto.getImageUrls();

        int order = 0;
        for (String url : newUrls) {
            BoardImage exist = activeMap.remove(url);
            if (exist != null) {
                exist.setSortOrder(order++);
                exist.activate(); // 혹시 비활성 상태였다면 활성화
            } else {
                board.getImages().add(BoardImage.builder()
                        .board(board).imageUrl(url).sortOrder(order++).build());
            }
        }
        // activeMap에 남은 것들은 제거된 이미지 → 비활성화
        for (BoardImage removed : activeMap.values()) {
            removed.deactivate();
            removed.setSortOrder(null);
        }
    }

    @Transactional
    public void deleteBoard(String email, Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        if (!board.getAuthor().getEmail().equals(email)) {
            throw new IllegalArgumentException("작성자만 삭제할 수 있습니다.");
        }

        // 실제 삭제 대신 비활성화를 권장하려면 아래 메서드 사용
        deactivateOwnBoard(email, boardId);
    }

    @Transactional
    public void deactivateOwnBoard(String email, Long boardId) {
        Board board = boardRepository.findByIdAndActiveTrue(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없거나 이미 삭제되었습니다."));

        if (!board.getAuthor().getEmail().equals(email)) {
            throw new IllegalArgumentException("작성자만 삭제할 수 있습니다.");
        }
        board.deactivate();

        // 댓글/북마크/좋아요 연쇄 비활성화
        List<Comment> comments = commentRepository.findByBoardId(boardId);
        for (Comment comment : comments) comment.deactivate();

        List<Bookmark> bookmarks = bookmarkRepository.findByBoard(board);
        for (Bookmark bookmark : bookmarks) bookmark.deactivate();

        List<Like> likes = likeRepository.findByBoard(board);
        for (Like like : likes) like.deactivate();

        // 이미지도 비활성화
        for (BoardImage img : board.getImages()) {
            if (img.isActive()) img.deactivate();
        }
    }

    @Transactional
    public void deactivateBoardByAdmin(Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        board.deactivate();
    }

    private void deactivateBoardAndAssociations(Board board) {
        board.deactivate();
        bookmarkRepository.findByBoard(board).forEach(Bookmark::deactivate);
        likeRepository.findByBoard(board).forEach(Like::deactivate);
        // 이미지도 함께 비활성화
        board.getImages().forEach(img -> { if (img.isActive()) img.deactivate(); });
    }

    @Transactional
    public void deactivateAllBoardsByUser(User user) {
        List<Board> boards = boardRepository.findByAuthor(user);
        for (Board board : boards) {
            deactivateBoardAndAssociations(board);
        }
    }

    @Transactional(readOnly = true)
    public Page<BoardResponseDto> searchBoards(BoardSearchConditionDto condition, Pageable pageable) {
        Specification<Board> spec = BoardSpecification.search(condition);
        Page<Board> boards = boardRepository.findAll(spec, pageable);
        return boards.map(board -> BoardResponseDto.builder()
                .id(board.getId())
                .title(board.getTitle())
                .content(board.getContent())
                .category(board.getCategory())
                .authorEmail(board.getAuthor().getEmail())
                .authorNickname(board.getAuthor().getNickname())
                .authorId(board.getAuthor().getId())
                .viewCount(board.getViewCount())
                .createdAt(board.getCreatedAt())
                .updatedAt(board.getUpdatedAt())
                .imageUrls(board.getImages().stream()
                        .filter(BoardImage::isActive)
                        .sorted(Comparator.comparing(i -> i.getSortOrder() == null ? Integer.MAX_VALUE : i.getSortOrder()))
                        .map(BoardImage::getImageUrl)
                        .collect(Collectors.toList()))
                .likeCount(0)
                .isLiked(false)
                .build());
    }
}
