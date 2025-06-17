package com.stackflov.controller;

import com.stackflov.dto.BookmarkRequestDto;
import com.stackflov.dto.BookmarkResponseDto;
import com.stackflov.jwt.JwtProvider;
import com.stackflov.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;
    private final JwtProvider jwtProvider;

    // 북마크 추가 (POST /bookmarks)
    @PostMapping
    public ResponseEntity<BookmarkResponseDto> addBookmark(
            @RequestAttribute("email") String userEmail, // JWT 필터에서 이메일 가져오기
            @RequestBody BookmarkRequestDto requestDto) {
        try {
            BookmarkResponseDto response = bookmarkService.addBookmark(userEmail, requestDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build(); // 또는 e.getMessage() 포함
        }
    }

    // 북마크 삭제 (DELETE /bookmarks)
    @DeleteMapping
    public ResponseEntity<Void> removeBookmark(
            @RequestAttribute("email") String userEmail, // JWT 필터에서 이메일 가져오기
            @RequestBody BookmarkRequestDto requestDto) {
        try {
            bookmarkService.removeBookmark(userEmail, requestDto);
            return ResponseEntity.noContent().build(); // 204 No Content
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build(); // 또는 e.getMessage() 포함
        }
    }

    // 내 북마크 목록 조회 (GET /bookmarks/my)
    @GetMapping("/my")
    public ResponseEntity<List<BookmarkResponseDto>> getUserBookmarks(
            @RequestAttribute("email") String userEmail) { // JWT 필터에서 이메일 가져오기
        List<BookmarkResponseDto> bookmarks = bookmarkService.getUserBookmarks(userEmail);
        return ResponseEntity.ok(bookmarks);
    }

    // 특정 게시글 북마크 여부 확인 (GET /bookmarks/board/{boardId}/check)
    @GetMapping("/board/{boardId}/check")
    public ResponseEntity<Boolean> isBookmarked(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable Long boardId) {

        String email = null;
        if (token != null && token.startsWith("Bearer ")) {
            String rawToken = token.substring(7);
            if (jwtProvider.validateToken(rawToken)) {
                email = jwtProvider.getEmail(rawToken);
            }
        }

        boolean bookmarked = bookmarkService.isBookmarked(email, boardId); // email == null이면 비회원
        return ResponseEntity.ok(bookmarked);
    }
}