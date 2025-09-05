package com.stackflov.controller;

import com.stackflov.dto.*;
import com.stackflov.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    @PostMapping
    public ResponseEntity<?> createBoard(@RequestBody BoardRequestDto dto,
                                         @AuthenticationPrincipal String email) {
        Long boardId = boardService.createBoard(email, dto);
        return ResponseEntity.ok(boardId);
    }

    @GetMapping("/{boardId}")
    public ResponseEntity<BoardResponseDto> getBoard(
            @PathVariable Long boardId,
            @AuthenticationPrincipal String email // 비로그인 허용: null 가능
    ) {
        BoardResponseDto response = boardService.getBoard(boardId, email);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<BoardListResponseDto>> getBoards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal String email
    ) {
        Page<BoardListResponseDto> boards = boardService.getBoards(page, size, email);
        return ResponseEntity.ok(boards);
    }

    @PutMapping("/{boardId}")
    public ResponseEntity<?> updateBoard(@PathVariable Long boardId,
                                         @RequestBody BoardUpdateRequestDto dto,
                                         @AuthenticationPrincipal String email) {
        boardService.updateBoard(email, boardId, dto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{boardId}")
    public ResponseEntity<?> deleteBoard(@PathVariable Long boardId,
                                         @AuthenticationPrincipal String email) {
        boardService.deactivateOwnBoard(email, boardId);
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
}
