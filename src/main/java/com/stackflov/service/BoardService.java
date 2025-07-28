package com.stackflov.service;

import com.stackflov.domain.Board;
import com.stackflov.domain.BoardImage;
import com.stackflov.domain.Bookmark;
import com.stackflov.domain.User;
import com.stackflov.dto.BoardListResponseDto;
import com.stackflov.dto.BoardRequestDto;
import com.stackflov.dto.BoardResponseDto;
import com.stackflov.dto.BoardUpdateRequestDto;
import com.stackflov.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final BoardImageRepository boardImageRepository;
    private final UserRepository userRepository;
    private final BookmarkRepository bookmarkRepository;
    private final LikeRepository likeRepository;

    public Long createBoard(String email, BoardRequestDto dto) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        Board board = Board.builder()
                .author(user)
                .title(dto.getTitle())
                .content(dto.getContent())
                .category(dto.getCategory())
                .viewCount(0)
                .build();

        Board saved = boardRepository.save(board);
        List<BoardImage> images = dto.getImageUrls().stream()
                .map(url -> BoardImage.builder()
                        .board(saved)
                        .imageUrl(url)
                        .build())
                .toList();

        boardImageRepository.saveAll(images);

        return saved.getId();
    }

    @Transactional
    public BoardResponseDto getBoard(Long boardId, String email) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        board.increaseViewCount(); // 조회수 증가

        long likeCount = likeRepository.countByBoard(board);
        boolean isLiked = false;

        // 로그인한 사용자(email != null)라면, 좋아요 여부 확인
        if (email != null) {
            // 이메일로 사용자를 찾고, 사용자가 존재하면 좋아요 여부를 확인
            userRepository.findByEmail(email).ifPresent(user -> {
                // isLiked 값을 직접 수정할 수 없으므로, 별도의 변수를 사용하거나 결과를 반환받아야 함
                // 하지만 여기서는 isLiked가 외부 변수이므로 직접 접근이 안됨.
                // 따라서 아래와 같이 수정하는 것이 더 안전함
            });
            // 위 람다식은 isLiked 변수를 직접 수정할 수 없어 문제가 되므로, 아래와 같이 간단하게 처리
            isLiked = userRepository.findByEmail(email)
                    .map(user -> likeRepository.existsByUserAndBoard(user, board))
                    .orElse(false);
        }

        List<String> imageUrls = board.getImages().stream()
                .map(BoardImage::getImageUrl)
                .collect(Collectors.toList()); // .toList()도 가능

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
                .likeCount(likeCount)
                .isLiked(isLiked)
                .build();
    }
    public Page<BoardListResponseDto> getBoards(int page, int size, String userEmail) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Board> boards = boardRepository.findAll(pageable);
        Set<Long> bookmarkedBoardIds = new HashSet<>();
        if (userEmail != null) {
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
            List<Bookmark> bookmarks = bookmarkRepository.findByUser(user);
            bookmarkedBoardIds = bookmarks.stream()
                    .map(b -> b.getBoard().getId())
                    .collect(Collectors.toSet());
        }
        return boards.map(board -> BoardListResponseDto.builder()
                .id(board.getId())
                .title(board.getTitle())
                .authorEmail(board.getAuthor().getEmail())
                .authorNickname(board.getAuthor().getNickname())  // ✅ 추가
                .authorId(board.getAuthor().getId())              // ✅ 추가
                .category(board.getCategory())
                .thumbnailUrl(board.getImages().isEmpty() ? null : board.getImages().get(0).getImageUrl())
                .viewCount(board.getViewCount())        // ✅ 추가
                .createdAt(board.getCreatedAt())        // ✅ 추가
                .updatedAt(board.getUpdatedAt())
                .build());
    }

    @Transactional
    public void updateBoard(String email, Long boardId, BoardUpdateRequestDto dto) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        if (!board.getAuthor().getEmail().equals(email)) {
            throw new IllegalArgumentException("작성자만 수정할 수 있습니다.");
        }

        // 게시글 제목, 내용, 카테고리 수정
        board.update(dto.getTitle(), dto.getContent(), dto.getCategory());

        // 기존 이미지 삭제
        boardImageRepository.deleteAll(board.getImages());

        // 이미지 URL이 null일 경우 빈 리스트 처리
        List<BoardImage> newImages = (dto.getImageUrls() == null ? new ArrayList<>() : dto.getImageUrls()).stream()
                .map(url -> BoardImage.builder()
                        .board(board)
                        .imageUrl((String) url)
                        .build())
                .collect(Collectors.toList());

        // 새로운 이미지 URL을 저장
        boardImageRepository.saveAll(newImages);
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
}