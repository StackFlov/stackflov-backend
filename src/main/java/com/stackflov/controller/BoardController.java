package com.stackflov.controller;

import com.stackflov.dto.*;
import com.stackflov.jwt.JwtProvider;
import com.stackflov.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;
    private final JwtProvider jwtProvider;
    @PostMapping
    public ResponseEntity<?> createBoard(@RequestBody BoardRequestDto dto,
                                         @RequestHeader("Authorization") String accessToken) {
        String token = accessToken.replace("Bearer ", "");
        String email = jwtProvider.getEmail(token);

        Long boardId = boardService.createBoard(email, dto);
        return ResponseEntity.ok(boardId);
    }

    private Long extractUserIdFromToken(String token) {
        // TODO: JWT 토큰 파싱해서 실제 유저 ID 반환하는 코드 작성
        return 1L; // 테스트용 목값 (임시 하드코딩)
    }

    @GetMapping("/{boardId}")
    public ResponseEntity<BoardResponseDto> getBoard(
            @PathVariable Long boardId,
            @RequestHeader(value = "Authorization", required = false) String accessToken) { // required = false로 변경

        String email = null;
        // 토큰이 존재하고, 유효한 경우에만 이메일 추출
        if (accessToken != null && accessToken.startsWith("Bearer ")) {
            String token = accessToken.substring(7);
            if (jwtProvider.validateToken(token)) { // validateToken 메소드가 JwtProvider에 있다고 가정
                email = jwtProvider.getEmail(token);
            }
        }

        // email 정보를 서비스로 전달 (email이 null이면 비로그인 사용자)
        BoardResponseDto response = boardService.getBoard(boardId, email);
        return ResponseEntity.ok(response);
    }
    @GetMapping
    public ResponseEntity<Page<BoardListResponseDto>> getBoards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestAttribute(value = "email", required = false) String email//10에서 바꾸고 싶을 때 여기서 바꾸면 됨
    ) {
        Page<BoardListResponseDto> boards = boardService.getBoards(page, size, email);
        return ResponseEntity.ok(boards);
    }
    @PutMapping("/{boardId}")
    public ResponseEntity<?> updateBoard(@PathVariable Long boardId,
                                         @RequestBody BoardUpdateRequestDto dto,
                                         @RequestHeader("Authorization") String accessToken) {
        String token = accessToken.replace("Bearer ", "");
        String email = jwtProvider.getEmail(token);

        boardService.updateBoard(email, boardId, dto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{boardId}")
    public ResponseEntity<?> deleteBoard(@PathVariable Long boardId,
                                         @RequestHeader("Authorization") String accessToken) {
        String token = accessToken.replace("Bearer ", "");
        String email = jwtProvider.getEmail(token);

        boardService.deactivateOwnBoard(email, boardId);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/boards/search")
    public ResponseEntity<Page<BoardResponseDto>> searchBoards(
            @ModelAttribute BoardSearchConditionDto condition,
            Pageable pageable
    ) {
        Page<BoardResponseDto> results = boardService.searchBoards(condition, pageable);
        return ResponseEntity.ok(results);
    }
}