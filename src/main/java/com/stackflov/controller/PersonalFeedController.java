package com.stackflov.controller;

import com.stackflov.domain.UserReco;
import com.stackflov.repository.UserRecoRepository;
import com.stackflov.config.CustomUserPrincipal;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feed")
public class PersonalFeedController {

    private final UserRecoRepository userRecoRepository;

    public PersonalFeedController(UserRecoRepository userRecoRepository) {
        this.userRecoRepository = userRecoRepository;
    }

    public record RecoItem(Long boardId, double score) {}

    @GetMapping("/personal")
    public List<RecoItem> personal(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam(defaultValue = "20") int size
    ) {
        Long userId = principal.getId();
        return userRecoRepository.findTopByUserId(userId, PageRequest.of(0, size))
                .stream()
                .map(r -> new RecoItem(r.getId().getBoardId(), r.getScore()))
                .toList();
    }
}