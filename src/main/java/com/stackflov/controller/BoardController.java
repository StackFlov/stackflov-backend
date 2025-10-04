package com.stackflov.controller;

import com.stackflov.config.CustomUserPrincipal;
import com.stackflov.dto.*;
import com.stackflov.service.BoardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Boards", description = "게시글 조회/검색/작성/수정/삭제 API")
@RestController
@RequestMapping("/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    @Operation(summary = "게시글 단건 조회", description = "boardId로 게시글 상세를 조회합니다. 로그인 시 사용자 컨텍스트에 따라 일부 필드가 달라질 수 있습니다.")
    @GetMapping("/{boardId}")
    public ResponseEntity<BoardResponseDto> getBoard(
            @PathVariable Long boardId,
            @AuthenticationPrincipal @Nullable CustomUserPrincipal principal) {
        String email = (principal != null) ? principal.getEmail() : null;
        BoardResponseDto response = boardService.getBoard(boardId, email);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "게시글 목록 조회", description = "페이지/사이즈로 게시글 목록을 페이징 조회합니다.")
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

    @Operation(summary = "게시글 수정 (멀티파트)", description = "`data` 파트에 JSON, `images` 파트에 파일을 담아 전송합니다.")
    @PutMapping(value = "/{boardId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateBoard(
            @PathVariable Long boardId,
            @RequestPart("data") BoardUpdateRequestDto dto,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        boardService.updateBoard(principal.getEmail(), boardId, dto, images);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "게시글 삭제", description = "본인 소유의 게시글을 비활성화(삭제)합니다.")
    @DeleteMapping("/{boardId}")
    public ResponseEntity<?> deleteBoard(@PathVariable Long boardId,
                                         @AuthenticationPrincipal CustomUserPrincipal principal) {
        boardService.deactivateOwnBoard(principal.getEmail(), boardId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "게시글 검색", description = "검색 조건(`BoardSearchConditionDto`)과 페이징으로 게시글을 검색합니다.")
    @GetMapping("/search")
    public ResponseEntity<Page<BoardResponseDto>> searchBoards(
            @ModelAttribute BoardSearchConditionDto condition,
            Pageable pageable
    ) {
        Page<BoardResponseDto> results = boardService.searchBoards(condition, pageable);
        return ResponseEntity.ok(results);
    }

    @Operation(summary = "게시글 작성 (멀티파트)", description = "`data` 파트에 JSON, `images` 파트에 파일을 담아 게시글을 생성합니다.")
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

    @Operation(summary = "해시태그로 게시글 목록 조회", description = "지정한 해시태그에 해당하는 게시글 목록을 페이징으로 조회합니다.")
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
