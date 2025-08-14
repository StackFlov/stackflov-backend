package com.stackflov.service;

import com.stackflov.domain.Board;
import com.stackflov.domain.Bookmark;
import com.stackflov.domain.User;
import com.stackflov.dto.BookmarkRequestDto;
import com.stackflov.dto.BookmarkResponseDto;
import com.stackflov.repository.BoardRepository;
import com.stackflov.repository.BookmarkRepository;
import com.stackflov.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;

    // 북마크 추가
    @Transactional
    public BookmarkResponseDto addBookmark(String userEmail, BookmarkRequestDto requestDto) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Board board = boardRepository.findById(requestDto.getBoardId())
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        // 중복 북마크 방지
        if (bookmarkRepository.findByUserAndBoard(user, board).isPresent()) {
            throw new IllegalArgumentException("이미 북마크된 게시글입니다.");
        }

        Bookmark bookmark = Bookmark.builder()
                .user(user)
                .board(board)
                .build();

        Bookmark savedBookmark = bookmarkRepository.save(bookmark);
        return new BookmarkResponseDto(savedBookmark);
    }

    // 북마크 삭제
    @Transactional
    public void removeBookmark(String userEmail, BookmarkRequestDto requestDto) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Board board = boardRepository.findById(requestDto.getBoardId())
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        Bookmark bookmark = bookmarkRepository.findByUserAndBoard(user, board)
                .orElseThrow(() -> new IllegalArgumentException("북마크를 찾을 수 없습니다."));

        bookmarkRepository.delete(bookmark);
    }

    // 특정 사용자의 모든 북마크 조회
    @Transactional(readOnly = true) // 읽기 전용 트랜잭션으로 성능 최적화
    public List<BookmarkResponseDto> getUserBookmarks(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        List<Bookmark> bookmarks = bookmarkRepository.findByUser(user);
        return bookmarks.stream()
                .map(BookmarkResponseDto::new) // Bookmark 엔티티를 DTO로 변환
                .collect(Collectors.toList());
    }

    // 특정 사용자가 특정 게시글을 북마크했는지 확인
    @Transactional(readOnly = true)
    public boolean isBookmarked(String userEmail, Long boardId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        return bookmarkRepository.findByUserAndBoard(user, board).isPresent();
    }

    @Transactional
    public void deactivateAllBookmarksByUser(User user) {
        List<Bookmark> bookmarks = bookmarkRepository.findByUser(user);
        bookmarks.forEach(Bookmark::deactivate);
    }
}