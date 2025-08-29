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
    private final LikeRepository likeRepository; // BoardListResponseDto를 위해 필요
    private final FollowRepository followRepository;

    // 사용자 프로필 정보 조회
    @Transactional(readOnly = true)
    public UserProfileDto getUserProfile(Long userId) {
        User user = userRepository.findByIdAndActiveTrue(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없거나 비활성화된 계정입니다."));

        // 👇 팔로워와 팔로잉 수를 계산하는 로직 추가
        long followerCount = followRepository.countByFollowedId(userId);
        long followingCount = followRepository.countByFollowerId(userId);

        // 👇 수정된 DTO 생성자로 객체를 생성하여 반환
        return new UserProfileDto(user, followerCount, followingCount);
    }

    // 특정 사용자가 작성한 게시글 목록 조회
    @Transactional(readOnly = true)
    public Page<BoardListResponseDto> getBoardsByUser(Long userId, Pageable pageable) {
        User user = userRepository.findByIdAndActiveTrue(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없거나 비활성화된 계정입니다."));

        Page<Board> boards = boardRepository.findByAuthorAndActiveTrue(user, pageable);

        // 기존 BoardListResponseDto를 재사용하여 변환
        return boards.map(board -> BoardListResponseDto.builder()
                .id(board.getId())
                .title(board.getTitle())
                .authorNickname(board.getAuthor().getNickname())
                .authorId(board.getAuthor().getId())
                .thumbnailUrl(board.getImages().isEmpty() ? null : board.getImages().get(0).getImageUrl())
                .viewCount(board.getViewCount())
                .likeCount(likeRepository.countByBoardAndActiveTrue(board))
                .createdAt(board.getCreatedAt())
                .build());
    }
}