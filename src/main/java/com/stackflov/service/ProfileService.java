package com.stackflov.service;

import com.stackflov.domain.Board;
import com.stackflov.domain.User;
import com.stackflov.dto.BoardListResponseDto;
import com.stackflov.dto.UserProfileDto;
import com.stackflov.repository.BoardRepository;
import com.stackflov.repository.LikeRepository;
import com.stackflov.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.stackflov.repository.FollowRepository;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final LikeRepository likeRepository;
    private final FollowRepository followRepository;

    @Transactional(readOnly = true)
    public UserProfileDto getUserProfile(Long userId) {
        User user = userRepository.findByIdAndActiveTrue(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없거나 비활성화된 계정입니다."));

        long followerCount = followRepository.countByFollowedIdAndActiveTrue(userId);
        long followingCount = followRepository.countByFollowerIdAndActiveTrue(userId);

        return new UserProfileDto(user, followerCount, followingCount);
    }

    @Transactional(readOnly = true)
    public Page<BoardListResponseDto> getBoardsByUser(Long userId, Pageable pageable) {
        User user = userRepository.findByIdAndActiveTrue(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없거나 비활성화된 계정입니다."));

        Page<Board> boards = boardRepository.findByAuthorAndActiveTrue(user, pageable);

        return boards.map(board -> BoardListResponseDto.builder()
                .id(board.getId())
                .title(board.getTitle())
                .authorNickname(board.getAuthor().getNickname())
                .authorId(board.getAuthor().getId())
                .thumbnailUrl(board.getImages().stream()
                        .filter(img -> img.isActive())
                        .sorted(java.util.Comparator.comparing(i -> i.getSortOrder() == null ? Integer.MAX_VALUE : i.getSortOrder()))
                        .map(i -> i.getImageUrl())
                        .findFirst().orElse(null))
                .viewCount(board.getViewCount())
                .likeCount(likeRepository.countByBoardAndActiveTrue(board))
                .createdAt(board.getCreatedAt())
                .build());
    }
}
