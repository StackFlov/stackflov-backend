package com.stackflov.controller;

import com.stackflov.config.CustomUserPrincipal;
import com.stackflov.dto.BookmarkRequestDto;
import com.stackflov.dto.BookmarkResponseDto;
import com.stackflov.service.BookmarkService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Bookmarks", description = "북마크 추가/삭제/조회")
@RestController
@RequestMapping("/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @PostMapping
    public ResponseEntity<BookmarkResponseDto> addBookmark(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestBody BookmarkRequestDto requestDto) {
        try {
            BookmarkResponseDto response = bookmarkService.addBookmark(principal.getEmail(), requestDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping
    public ResponseEntity<Void> removeBookmark(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestBody BookmarkRequestDto requestDto) {
        try {
            bookmarkService.removeBookmark(principal.getEmail(), requestDto);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/my")
    public ResponseEntity<List<BookmarkResponseDto>> getUserBookmarks(
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        List<BookmarkResponseDto> bookmarks = bookmarkService.getUserBookmarks(principal.getEmail());
        return ResponseEntity.ok(bookmarks);
    }

    @GetMapping("/board/{boardId}/check")
    public ResponseEntity<Boolean> isBookmarked(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long boardId) {
        String email = (principal == null) ? null : principal.getEmail();
        boolean bookmarked = bookmarkService.isBookmarked(email, boardId); // email == null → 게스트 처리
        return ResponseEntity.ok(bookmarked);
    }
}
