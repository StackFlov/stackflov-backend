package com.stackflov.controller;

import com.stackflov.dto.BoardListResponseDto;
import com.stackflov.service.FeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;

    @GetMapping("/feed")
    public ResponseEntity<Page<BoardListResponseDto>> getFeed(
            @AuthenticationPrincipal String userEmail,
            Pageable pageable
    ) {
        Page<BoardListResponseDto> feed = feedService.getFeed(userEmail, pageable);
        return ResponseEntity.ok(feed);
    }
}
