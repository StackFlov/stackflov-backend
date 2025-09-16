package com.stackflov.controller;

import com.stackflov.config.CustomUserPrincipal;
import com.stackflov.dto.*;
import com.stackflov.service.BoardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.lang.Nullable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    @GetMapping("/{boardId}")
    public ResponseEntity<BoardResponseDto> getBoard(
            @PathVariable Long boardId,
            @AuthenticationPrincipal @Nullable CustomUserPrincipal principal) {
        String email = (principal != null) ? principal.getEmail() : null;
        BoardResponseDto response = boardService.getBoard(boardId, email);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<BoardListResponseDto>> getBoards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal @Nullable CustomUserPrincipal principal
    ) {
        String email = (principal != null) ? principal.getEmail() : null;
        Page<BoardListResponseDto> boards = boardService.getBoards(page, size, email);
        return ResponseEntity.ok(boards);
    }

    @PutMapping("/{boardId}")
    public ResponseEntity<?> updateBoard(@PathVariable Long boardId,
                                         @RequestBody BoardUpdateRequestDto dto,
                                         @AuthenticationPrincipal CustomUserPrincipal principal) {
        boardService.updateBoard(principal.getEmail(), boardId, dto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{boardId}")
    public ResponseEntity<?> deleteBoard(@PathVariable Long boardId,
                                         @AuthenticationPrincipal CustomUserPrincipal principal) {
        boardService.deactivateOwnBoard(principal.getEmail(), boardId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<Page<BoardResponseDto>> searchBoards(
            @ModelAttribute BoardSearchConditionDto condition,
            Pageable pageable
    ) {
        Page<BoardResponseDto> results = boardService.searchBoards(condition, pageable);
        return ResponseEntity.ok(results);
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping(value = "/multipart", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createBoardWithFiles(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestPart("data") BoardCreateRequestDto data,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        String email = principal.getEmail();
        Long boardId = boardService.createBoardWithFiles(email, data, images);
        return ResponseEntity.ok(boardId);
    }

    @GetMapping("/tags/{tagName}")
    public ResponseEntity<Page<BoardListResponseDto>> getBoardsByHashtag(
            @PathVariable String tagName,
            Pageable pageable,
            @AuthenticationPrincipal @Nullable CustomUserPrincipal principal
    ) {
        String email = (principal != null) ? principal.getEmail() : null;
        Page<BoardListResponseDto> boards = boardService.getBoardsByHashtag(tagName, pageable, email);
        return ResponseEntity.ok(boards);
    }

}
