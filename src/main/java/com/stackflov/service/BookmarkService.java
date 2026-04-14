package com.stackflov.service;

import com.stackflov.domain.Board;
import com.stackflov.domain.Bookmark;
import com.stackflov.domain.Review;
import com.stackflov.domain.User;
import com.stackflov.dto.BookmarkRequestDto;
import com.stackflov.dto.BookmarkResponseDto;
import com.stackflov.repository.BoardRepository;
import com.stackflov.repository.BookmarkRepository;
import com.stackflov.repository.ReviewRepository;
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
    private final UserService userService;
    private final ReviewRepository reviewRepository;

    @Transactional
    public BookmarkResponseDto addBookmark(String userEmail, BookmarkRequestDto requestDto) {
        User user = userService.getValidUserByEmail(userEmail);
        Bookmark bookmark;
        if (requestDto.isBoardBookmark()) {
            Board board = boardRepository.findById(requestDto.getBoardId())
                    .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

            // 중복 체크 (ExistsBy를 쓰면 더 빠릅니다)
            if (bookmarkRepository.existsByUserAndBoardAndActiveTrue(user, board)) {
                throw new IllegalArgumentException("이미 북마크된 게시글입니다.");
            }

            bookmark = Bookmark.builder()
                    .user(user)
                    .board(board)
                    .build();

        } else if (requestDto.isReviewBookmark()) {
            Review review = reviewRepository.findById(requestDto.getReviewId())
                    .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));

            // 중복 체크
            if (bookmarkRepository.existsByUserAndReviewAndActiveTrue(user, review)) {
                throw new IllegalArgumentException("이미 북마크된 리뷰입니다.");
            }

            bookmark = Bookmark.builder()
                    .user(user)
                    .review(review)
                    .build();
        }
        else {
            throw new IllegalArgumentException("북마크할 대상(게시글 또는 리뷰)이 없습니다.");
        }

        Bookmark savedBookmark = bookmarkRepository.save(bookmark);
        return new BookmarkResponseDto(savedBookmark);
    }

    @Transactional
    public void removeBookmark(String userEmail, BookmarkRequestDto requestDto) {
        User user = userService.getValidUserByEmail(userEmail);
        Bookmark bookmark;

        if (requestDto.isBoardBookmark()) {
            Board board = boardRepository.findById(requestDto.getBoardId())
                    .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
            bookmark = bookmarkRepository.findByUserAndBoard(user, board)
                    .orElseThrow(() -> new IllegalArgumentException("북마크를 찾을 수 없습니다."));
        }else {
            Review review = reviewRepository.findById(requestDto.getReviewId())
                    .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));
            bookmark = bookmarkRepository.findByUserAndReview(user, review)
                    .orElseThrow(() -> new IllegalArgumentException("북마크를 찾을 수 없습니다."));
        }

        bookmarkRepository.delete(bookmark);
    }

    @Transactional(readOnly = true)
    public List<BookmarkResponseDto> getUserBookmarks(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        List<Bookmark> bookmarks = bookmarkRepository.findByUser(user);
        return bookmarks.stream()
                .map(BookmarkResponseDto::new)
                .collect(Collectors.toList());
    }

    // 변경: 게스트(null email)면 false 반환
    @Transactional(readOnly = true)
    public boolean isBookmarked(String userEmail, Long boardId) {
        if (userEmail == null || userEmail.isBlank()) {
            return false;
        }
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
