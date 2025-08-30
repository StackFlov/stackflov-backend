package com.stackflov.service;

import com.stackflov.domain.Board;
import com.stackflov.domain.Follow;
import com.stackflov.domain.User;
import com.stackflov.dto.BoardListResponseDto;
import com.stackflov.repository.BoardRepository;
import com.stackflov.repository.FollowRepository;
import com.stackflov.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final BoardRepository boardRepository;

    public Page<BoardListResponseDto> getFeed(String userEmail, Pageable pageable) {
        // 1. 현재 로그인한 사용자 정보를 가져옵니다.
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 2. 현재 사용자가 팔로우하는 모든 사람들의 목록을 가져옵니다.
        List<Follow> followingRelations = followRepository.findByFollowerId(currentUser.getId());
        List<User> followingUsers = followingRelations.stream()
                .map(Follow::getFollowed)
                .collect(Collectors.toList());

        // 3. 팔로우하는 사람들이 쓴 게시글들을 최신순으로 조회합니다.
        Page<Board> feedBoards = boardRepository.findByAuthorInOrderByCreatedAtDesc(followingUsers, pageable);

        // 4. 조회된 게시글들을 DTO로 변환하여 반환합니다.
        return feedBoards.map(board -> new BoardListResponseDto(board)); // BoardListResponseDto에 Board를 받는 생성자 필요
    }
}