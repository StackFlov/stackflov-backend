package com.stackflov.service;

import com.stackflov.domain.Board;
import com.stackflov.domain.BoardImage;
import com.stackflov.domain.User;
import com.stackflov.dto.BoardListResponseDto;
import com.stackflov.dto.BoardRequestDto;
import com.stackflov.dto.BoardResponseDto;
import com.stackflov.dto.BoardUpdateRequestDto;
import com.stackflov.jwt.JwtProvider;
import com.stackflov.repository.BoardImageRepository;
import com.stackflov.repository.BoardRepository;
import com.stackflov.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final BoardImageRepository boardImageRepository;
    private final UserRepository userRepository;

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

    public BoardResponseDto getBoard(Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        List<String> imageUrls = board.getImages().stream()
                .map(image -> image.getImageUrl())
                .toList();

        return BoardResponseDto.builder()
                .id(board.getId())
                .title(board.getTitle())
                .content(board.getContent())
                .category(board.getCategory())
                .authorEmail(board.getAuthor().getEmail())
                .imageUrls(imageUrls)
                .build();
    }
    public Page<BoardListResponseDto> getBoards(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Board> boards = boardRepository.findAll(pageable);

        return boards.map(board -> BoardListResponseDto.builder()
                .id(board.getId())
                .title(board.getTitle())
                .authorEmail(board.getAuthor().getEmail())
                .category(board.getCategory())
                .thumbnailUrl(board.getImages().isEmpty() ? null : board.getImages().get(0).getImageUrl())
                .build());
    }

    @Transactional
    public void updateBoard(String email, Long boardId, BoardUpdateRequestDto dto) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        if (!board.getAuthor().getEmail().equals(email)) {
            throw new IllegalArgumentException("작성자만 수정할 수 있습니다.");
        }

        board.update(dto.getTitle(), dto.getContent(), dto.getCategory());

        // 기존 이미지 삭제 후 새 이미지 등록
        boardImageRepository.deleteAll(board.getImages());
        List<BoardImage> newImages = dto.getImageUrls().stream()
                .map(url -> BoardImage.builder()
                        .board(board)
                        .imageUrl(url)
                        .build())
                .toList();

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