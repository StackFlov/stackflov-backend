package com.stackflov.service;

import com.stackflov.domain.*;
import com.stackflov.dto.*;
import com.stackflov.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final BookmarkRepository bookmarkRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final S3Service s3Service;
    private final UserService userService;
    private final BannedWordService bannedWordService;
    private final MentionService mentionService;
    private final HashtagService hashtagService;

    @Value("${app.defaults.profile-image}")
    private String defaultProfileImage;

    // ✅ 단일 게시글 조회
    @Transactional
    public BoardResponseDto getBoard(Long boardId, String email) {
        Board board = boardRepository.findByIdAndActiveTrue(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없거나 삭제되었습니다."));


        board.increaseViewCount();

        boolean isLiked = userRepository.findByEmail(email)
                .map(user -> likeRepository.existsByUserAndBoardAndActiveTrue(user, board))
                .orElse(false);

        List<String> imageUrls = board.getImages().stream()
                .filter(BoardImage::isActive)
                .sorted(Comparator.comparing(i -> i.getSortOrder() == null ? Integer.MAX_VALUE : i.getSortOrder()))
                .map(img -> s3Service.publicUrl(img.getImageUrl())) // key → URL 변환
                .collect(Collectors.toList());

        String raw = board.getAuthor().getProfileImage(); // DB: key or null
        String authorProfileImageUrl = (raw == null || raw.isBlank())
                ? defaultProfileImage                       // 기본 CDN 이미지
                : s3Service.publicUrl(raw);

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
                .authorProfileImageUrl(authorProfileImageUrl)
                .build();
    }

    // ✅ 게시글 목록 조회
    @Transactional(readOnly = true)
    public Page<BoardListResponseDto> getBoards(int page, int size, String userEmail) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Board> boards = boardRepository.findAllByActiveTrue(pageable);

        Set<Long> bookmarkedBoardIds = new HashSet<>();
        User currentUser = null;
        if (userEmail != null) {
            currentUser = userRepository.findByEmail(userEmail).orElse(null);
            if (currentUser != null) {
                bookmarkRepository.findByUserAndActiveTrue(currentUser)
                        .forEach(b -> bookmarkedBoardIds.add(b.getBoard().getId()));
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
                        .map(img -> s3Service.publicUrl(img.getImageUrl()))
                        .findFirst().orElse(null))
                .viewCount(board.getViewCount())
                .createdAt(board.getCreatedAt())
                .updatedAt(board.getUpdatedAt())
                .likeCount(likeRepository.countByBoardAndActiveTrue(board))
                .isBookmarked(bookmarkedBoardIds.contains(board.getId()))
                .isLiked(finalCurrentUser != null && likeRepository.existsByUserAndBoardAndActiveTrue(finalCurrentUser, board))
                .build());
    }

    // ✅ 게시글 수정
    @Transactional
    public void updateBoard(String email, Long boardId, BoardUpdateRequestDto dto, List<MultipartFile> images) {
        Board board = boardRepository.findByIdAndActiveTrue(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없거나 삭제되었습니다."));

        if (!board.getAuthor().getEmail().equals(email)) {
            throw new IllegalArgumentException("작성자만 수정할 수 있습니다.");
        }

        if (bannedWordService.containsBannedWord(dto.getTitle()) ||
                bannedWordService.containsBannedWord(dto.getContent())) {
            throw new IllegalArgumentException("제목이나 내용에 금지된 단어가 포함되어 있습니다.");
        }

        board.update(dto.getTitle(), dto.getContent(), dto.getCategory());
        mentionService.processMentions(board.getAuthor(), dto.getContent(), board, null);
        hashtagService.processHashtags(dto.getContent(), board);

        // 삭제 처리
        List<String> removeUrls = Optional.ofNullable(dto.getRemoveImageUrls()).orElse(List.of());
        if (!removeUrls.isEmpty()) {
            Map<String, BoardImage> activeByKey = board.getImages().stream()
                    .filter(BoardImage::isActive)
                    .collect(Collectors.toMap(
                            img -> s3Service.extractKey(img.getImageUrl()), // ← 메서드명 통일
                            img -> img,
                            (a, b) -> a
                    ));

            for (String url : removeUrls) {
                String key = s3Service.extractKey(url);           // ← 여기도 동일
                BoardImage target = activeByKey.get(key);
                if (target != null && target.isActive()) {
                    target.deactivate();
                    target.setSortOrder(null);
                    s3Service.deleteByKey(key);
                }
            }
        }

        // 새 이미지 업로드
        if (images != null && !images.isEmpty()) {
            for (MultipartFile file : images) {
                if (file != null && !file.isEmpty()) {
                    String key = s3Service.upload(file, "images");
                    board.getImages().add(BoardImage.builder()
                            .board(board)
                            .imageUrl(key) // key만 저장
                            .build());
                }
            }
        }

        // 정렬 재조정
        AtomicInteger order = new AtomicInteger(0);
        board.getImages().stream()
                .filter(BoardImage::isActive)
                .sorted(Comparator.comparing(i -> i.getSortOrder() == null ? Integer.MAX_VALUE : i.getSortOrder()))
                .forEach(img -> img.setSortOrder(order.getAndIncrement()));
    }

    // ✅ 게시글 삭제 (작성자)
    @Transactional
    public void deleteBoard(String email, Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        if (!board.getAuthor().getEmail().equals(email)) {
            throw new IllegalArgumentException("작성자만 삭제할 수 있습니다.");
        }

        deactivateBoardAndAssociations(boardId);
    }

    // ✅ 게시글 비활성화 (작성자/관리자 공통)
    @Transactional
    public void deactivateBoardAndAssociations(Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        // 1) S3 삭제 (key/URL 섞여 있어도 동작)
        board.getImages().stream()
                .filter(BoardImage::isActive)
                .map(img -> s3Service.extractKey(img.getImageUrl())) // URL → key 안전 변환
                .filter(Objects::nonNull)
                .distinct()
                .forEach(key -> {
                    try { s3Service.deleteByKey(key); } catch (Exception ignore) {}
                });

        // 2) DB 소프트 삭제
        board.deactivate();
        commentRepository.findByBoardId(boardId).forEach(Comment::deactivate);
        bookmarkRepository.findByBoard(board).forEach(Bookmark::deactivate);
        likeRepository.findByBoard(board).forEach(Like::deactivate);
        board.getImages().forEach(img -> { if (img.isActive()) { img.deactivate(); img.setSortOrder(null); }});
    }

    // ✅ 사용자 탈퇴 시 전체 비활성화
    @Transactional
    public void deactivateAllBoardsByUser(User user) {
        boardRepository.findByAuthor(user)
                .forEach(b -> deactivateBoardAndAssociations(b.getId()));
    }

    // ✅ 검색
    @Transactional(readOnly = true)
    public Page<BoardResponseDto> searchBoards(BoardSearchConditionDto condition, Pageable pageable) {
        Specification<Board> spec = BoardSpecification.search(condition);
        return boardRepository.findAll(spec, pageable)
                .map(board -> BoardResponseDto.builder()
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
                                .map(img -> s3Service.publicUrl(img.getImageUrl()))
                                .collect(Collectors.toList()))
                        .likeCount(likeRepository.countByBoardAndActiveTrue(board))
                        .isLiked(false)
                        .build());
    }

    // ✅ 생성
    @Transactional
    public Long createBoardWithFiles(String email, BoardCreateRequestDto data, List<MultipartFile> images) {
        User user = userService.getValidUserByEmail(email);

        if (bannedWordService.containsBannedWord(data.getTitle()) ||
                bannedWordService.containsBannedWord(data.getContent())) {
            throw new IllegalArgumentException("제목이나 내용에 금지된 단어가 포함되어 있습니다.");
        }

        Board board = Board.builder()
                .author(user)
                .title(data.getTitle())
                .content(data.getContent())
                .category(data.getCategory())
                .build();

        if (images != null && !images.isEmpty()) {
            AtomicInteger order = new AtomicInteger(0);
            images.stream()
                    .filter(f -> f != null && !f.isEmpty())
                    .forEach(file -> {
                        String key = s3Service.upload(file, "images");
                        board.getImages().add(BoardImage.builder()
                                .board(board)
                                .imageUrl(key)
                                .sortOrder(order.getAndIncrement())
                                .build());
                    });
        }

        Board saved = boardRepository.save(board);
        mentionService.processMentions(user, data.getContent(), saved, null);
        hashtagService.processHashtags(data.getContent(), saved);

        return saved.getId();
    }

    // ✅ 해시태그로 게시글 조회
    @Transactional(readOnly = true)
    public Page<BoardListResponseDto> getBoardsByHashtag(String tagName, Pageable pageable, String userEmail) {
        Page<Board> boards = boardRepository.findByHashtagName(tagName, pageable);

        User currentUser = (userEmail != null) ? userRepository.findByEmail(userEmail).orElse(null) : null;
        Set<Long> bookmarkedBoardIds = new HashSet<>();
        if (currentUser != null) {
            bookmarkRepository.findByUserAndActiveTrue(currentUser)
                    .forEach(b -> bookmarkedBoardIds.add(b.getBoard().getId()));
        }

        return boards.map(board -> BoardListResponseDto.builder()
                .id(board.getId())
                .title(board.getTitle())
                .authorNickname(board.getAuthor().getNickname())
                .authorId(board.getAuthor().getId())
                .thumbnailUrl(board.getImages().stream()
                        .filter(BoardImage::isActive)
                        .findFirst()
                        .map(img -> s3Service.publicUrl(img.getImageUrl()))
                        .orElse(null))
                .viewCount(board.getViewCount())
                .createdAt(board.getCreatedAt())
                .likeCount(likeRepository.countByBoardAndActiveTrue(board))
                .isBookmarked(bookmarkedBoardIds.contains(board.getId()))
                .isLiked(currentUser != null && likeRepository.existsByUserAndBoardAndActiveTrue(currentUser, board))
                .build());
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
}
