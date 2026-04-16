package com.stackflov.controller;

import com.stackflov.config.CustomUserPrincipal;
import com.stackflov.dto.BookmarkRequestDto;
import com.stackflov.dto.BookmarkResponseDto;
import com.stackflov.service.BookmarkService;
import io.swagger.v3.oas.annotations.Operation;
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

    @Operation(summary = "북마크 추가", description = "요청한 게시글을 내 북마크에 추가합니다.")
    @PostMapping
    public ResponseEntity<?> addBookmark(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestBody BookmarkRequestDto requestDto) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요한 서비스입니다.");
        }

        try {
            BookmarkResponseDto response = bookmarkService.addBookmark(principal.getEmail(), requestDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("서버 내부 오류가 발생했습니다.");
        }
    }

    @Operation(summary = "북마크 해제", description = "요청한 게시글을 내 북마크에서 제거합니다.")
    @DeleteMapping
    public ResponseEntity<?> removeBookmark(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestBody BookmarkRequestDto requestDto) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요한 서비스입니다.");
        }
        try {
            bookmarkService.removeBookmark(principal.getEmail(), requestDto);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "내 북마크 목록 조회", description = "현재 사용자 계정의 북마크 목록을 조회합니다.")
    @GetMapping("/my")
    public ResponseEntity<List<BookmarkResponseDto>> getUserBookmarks(
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        List<BookmarkResponseDto> bookmarks = bookmarkService.getUserBookmarks(principal.getEmail());
        return ResponseEntity.ok(bookmarks);
    }

    @Operation(summary = "게시글 북마크 여부 확인", description = "특정 게시글이 내 북마크에 있는지 확인합니다. 비로그인(게스트)도 호출 가능.")
    @GetMapping("/board/{boardId}/check")
    public ResponseEntity<Boolean> isBookmarked(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long boardId) {
        String email = (principal == null) ? null : principal.getEmail();
        boolean bookmarked = bookmarkService.isBookmarked(email, boardId); // email == null → 게스트 처리
        return ResponseEntity.ok(bookmarked);
    }
}
