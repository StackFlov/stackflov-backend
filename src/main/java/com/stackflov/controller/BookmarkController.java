package com.stackflov.controller;

import com.stackflov.dto.BookmarkRequestDto;
import com.stackflov.dto.BookmarkResponseDto;
import com.stackflov.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @PostMapping
    public ResponseEntity<BookmarkResponseDto> addBookmark(
            @AuthenticationPrincipal String userEmail,
            @RequestBody BookmarkRequestDto requestDto) {
        try {
            BookmarkResponseDto response = bookmarkService.addBookmark(userEmail, requestDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping
    public ResponseEntity<Void> removeBookmark(
            @AuthenticationPrincipal String userEmail,
            @RequestBody BookmarkRequestDto requestDto) {
        try {
            bookmarkService.removeBookmark(userEmail, requestDto);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/my")
    public ResponseEntity<List<BookmarkResponseDto>> getUserBookmarks(
            @AuthenticationPrincipal String userEmail) {
        List<BookmarkResponseDto> bookmarks = bookmarkService.getUserBookmarks(userEmail);
        return ResponseEntity.ok(bookmarks);
    }

    @GetMapping("/board/{boardId}/check")
    public ResponseEntity<Boolean> isBookmarked(
            @AuthenticationPrincipal String email,
            @PathVariable Long boardId) {
        boolean bookmarked = bookmarkService.isBookmarked(email, boardId); // email == null → 게스트 처리
        return ResponseEntity.ok(bookmarked);
    }
}
