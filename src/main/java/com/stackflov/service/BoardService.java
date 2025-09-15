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
import org.springframework.web.multipart.MultipartFile;

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
    private final S3Service s3Service;
    private final UserService userService;
    private final BannedWordService bannedWordService;

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

        if (bannedWordService.containsBannedWord(dto.getTitle()) || bannedWordService.containsBannedWord(dto.getContent())) {
            throw new IllegalArgumentException("제목이나 내용에 금지된 단어가 포함되어 있습니다.");
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
        // 1. 게시글 조회 및 소유권 확인 로직은 그대로 둡니다.
        Board board = boardRepository.findByIdAndActiveTrue(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없거나 이미 삭제되었습니다."));

        if (!board.getAuthor().getEmail().equals(email)) {
            throw new IllegalArgumentException("작성자만 삭제할 수 있습니다.");
        }

        // 2. 복잡했던 비활성화 로직을 모두 지우고, 방금 만든 메서드를 호출하는 한 줄만 남깁니다.
        deactivateBoardAndAssociations(boardId);
    }

    @Transactional
    public void deactivateBoardByAdmin(Long boardId) {
        // 👇 관리자 삭제 기능도 강력한 버전으로 변경
        deactivateBoardAndAssociations(boardId);
    }

    @Transactional
    public void deactivateBoardAndAssociations(Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        board.deactivate();
        commentRepository.findByBoardId(boardId).forEach(Comment::deactivate); // 댓글 포함
        bookmarkRepository.findByBoard(board).forEach(Bookmark::deactivate);
        likeRepository.findByBoard(board).forEach(Like::deactivate);
        board.getImages().forEach(img -> { if (img.isActive()) img.deactivate(); });
    }

    @Transactional
    public void deactivateAllBoardsByUser(User user) {
        List<Board> boards = boardRepository.findByAuthor(user);
        for (Board board : boards) {
            // 👇 새로 만든 public 메서드를 board ID로 호출합니다.
            deactivateBoardAndAssociations(board.getId());
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
    @Transactional
    public Long createBoardWithFiles(String email, BoardCreateRequestDto data, List<MultipartFile> images) {
        User user = userService.getValidUserByEmail(email);

        if (bannedWordService.containsBannedWord(data.getTitle()) || bannedWordService.containsBannedWord(data.getContent())) {
            throw new IllegalArgumentException("제목이나 내용에 금지된 단어가 포함되어 있습니다.");
        }

        // 1) 게시글 생성
        Board board = Board.builder()
                .author(user)
                .title(data.getTitle())
                .content(data.getContent())
                .category(data.getCategory())
                .build();

        // 2) 파일이 있으면 업로드 후 BoardImage 저장
        if (images != null && !images.isEmpty()) {
            AtomicInteger order = new AtomicInteger(0);
            List<BoardImage> boardImages = images.stream()
                    .filter(f -> f != null && !f.isEmpty())
                    .map(file -> {
                        String url = s3Service.upload(file, "images");
                        return BoardImage.builder()
                                .board(board)
                                .imageUrl(url)
                                .sortOrder(order.getAndIncrement())
                                .build();
                    })
                    .collect(Collectors.toList());
            board.getImages().addAll(boardImages);
        }

        Board savedBoard = boardRepository.save(board);
        return savedBoard.getId();
    }
}
