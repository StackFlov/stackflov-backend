package com.stackflov.controller;

import com.stackflov.dto.BoardRequestDto;
import com.stackflov.dto.BoardResponseDto;
import com.stackflov.jwt.JwtProvider;
import com.stackflov.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;
    private final JwtProvider jwtProvider;

    // 게시글 생성 (인증 필요)
    @PostMapping
    public ResponseEntity<BoardResponseDto> createBoard(@RequestBody BoardRequestDto dto,
                                                        @RequestHeader("Authorization") String accessToken) {
        String token = accessToken.replace("Bearer ", "");
        String email = jwtProvider.getEmail(token);

        BoardResponseDto boardResponse = boardService.createBoard(email, dto);
        return ResponseEntity.ok(boardResponse);
    }

    @GetMapping("/{boardId}")
    public ResponseEntity<BoardResponseDto> getBoard(@PathVariable Long boardId) {
        BoardResponseDto response = boardService.getBoard(boardId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<BoardResponseDto>> getBoards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort
    ) {
        Sort.Direction direction = Sort.Direction.fromString(sort[1]);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort[0]));

        Page<BoardResponseDto> boards = boardService.getBoards(pageable);
        return ResponseEntity.ok(boards);
    }

    @PutMapping("/{boardId}")
    public ResponseEntity<BoardResponseDto> updateBoard(@PathVariable Long boardId,
                                                        @RequestBody BoardRequestDto dto,
                                                        @RequestHeader("Authorization") String accessToken) {
        String token = accessToken.replace("Bearer ", "");
        String email = jwtProvider.getEmail(token);

        BoardResponseDto updatedBoard = boardService.updateBoard(boardId, email, dto);
        return ResponseEntity.ok(updatedBoard);
    }

    @DeleteMapping("/{boardId}")
    public ResponseEntity<Void> deleteBoard(@PathVariable Long boardId,
                                            @RequestHeader("Authorization") String accessToken) {
        String token = accessToken.replace("Bearer ", "");
        String email = jwtProvider.getEmail(token);

        boardService.deleteBoard(boardId, email);
        return ResponseEntity.noContent().build();
    }
}