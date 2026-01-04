package com.stackflov.service;

import com.stackflov.domain.Role;
import com.stackflov.domain.SocialType;
import com.stackflov.domain.User;
import com.stackflov.dto.*;
import com.stackflov.jwt.JwtProvider;
import com.stackflov.repository.BoardRepository;
import com.stackflov.repository.ReviewRepository;
import com.stackflov.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RedisService redisService;
    private final S3Service s3Service;
    private final BoardRepository boardRepository;
    private final ReviewRepository reviewRepository;
    private final FollowService followService;

    @Value("${app.defaults.profile-image}")
    private String defaultProfileImage;

    public TokenResponseDto login(LoginRequestDto requestDto) {
        User user = getValidUserByEmail(requestDto.getEmail());

        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        String accessToken = jwtProvider.createAccessToken(user.getEmail(), user.getRole().name());
        String refreshToken = jwtProvider.createRefreshToken(user.getEmail());

        redisService.save("RT:" + user.getEmail(), refreshToken, jwtProvider.REFRESH_TOKEN_EXPIRE_TIME);

        return new TokenResponseDto(accessToken, refreshToken);
    }
    public void register(SignupRequestDto signupRequestDto) {
        String email = signupRequestDto.getEmail();

        String input = signupRequestDto.getProfileImage();
        String profileKey = (input == null || input.isBlank())
                ? s3Service.extractKey(defaultProfileImage) // 👈 기본도 키로 저장
                : s3Service.extractKey(input);

        // ✅ 이메일 인증 여부 확인
        String verified = redisService.get("EMAIL_VERIFIED:" + email);
        if (!"true".equals(verified)) {
            throw new IllegalArgumentException("이메일 인증이 필요합니다.");
        }


        // ✅ 중복 이메일 확인
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(signupRequestDto.getPassword()))
                .nickname(signupRequestDto.getNickname())
                .profileImage(profileKey)              // ✅ 여기
                .socialType(SocialType.NONE)
                .socialId(signupRequestDto.getSocialId())
                .level(0)                           // 기본 고정
                .role(Role.USER)                    // 기본 고정
                .phoneNumber(signupRequestDto.getPhoneNumber())
                .address(signupRequestDto.getAddress())
                .build();
        userRepository.save(user);
    }
    public UserResponseDto getUserByEmail(String email) {
        User user = userRepository.findByEmailAndActiveTrue(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 탈퇴한 사용자입니다."));
        String fullProfileUrl = s3Service.publicUrl(user.getProfileImage());
        return new UserResponseDto(user, fullProfileUrl);
    }
    @Transactional
    public void updateUser(String email, UserUpdateRequestDto dto) {
        User user = getValidUserByEmail(email);

        if (dto.getNickname() != null) {
            user.updateNickname(dto.getNickname());
        }

        if (dto.getProfileImage() != null) {
            user.updateProfileImage(dto.getProfileImage());
        }

        if (dto.getPhoneNumber() != null) {              // ✅ 추가
            user.updatePhoneNumber(dto.getPhoneNumber());
        }

        if (dto.getAddress() != null) {                  // ✅ 추가
            user.updateAddress(dto.getAddress());
        }
    }
    @Transactional
    public void updatePassword(String email, PasswordUpdateRequestDto dto) {
        // 사용자 확인
        User user = getValidUserByEmail(email);

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 새 비밀번호로 변경
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
    }

    public User getValidUserByEmail(String email) {
        User user = userRepository.findByEmailAndActiveTrue(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 비활성화된 계정입니다."));

        if (user.getSuspensionEndDate() != null && user.getSuspensionEndDate().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("정지된 계정입니다. 해제일: " + user.getSuspensionEndDate());
        }
        return user;
    }

    @Transactional
    public UserProfileDetailResponseDto getUserProfileDetail(Long targetUserId, String requesterEmail) {
        // 1. 대상 사용자 조회
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

        if (targetUser.getRole() == Role.ADMIN) {
            throw new IllegalArgumentException("관리자 프로필은 조회할 수 없습니다.");
        }

        // 2. 게시글 리스트 조회 (최근순 5개 예시)
        List<BoardListResponseDto> boards = boardRepository.findByAuthorAndActiveTrue(
                targetUser,
                org.springframework.data.domain.PageRequest.of(0, 5, org.springframework.data.domain.Sort.by("createdAt").descending())
        ).stream().map(board -> BoardListResponseDto.builder()
                .id(board.getId())
                .title(board.getTitle())
                .category(board.getCategory())
                .viewCount(board.getViewCount())
                .createdAt(board.getCreatedAt())
                .thumbnailUrl(board.getImages().stream()
                        .filter(img -> img.isActive())
                        .findFirst()
                        .map(img -> s3Service.publicUrl(img.getImageUrl()))
                        .orElse(null))
                .build()
        ).toList();

        // 3. 리뷰 리스트 조회 (최근순 5개 예시)
        List<ReviewListResponseDto> reviews = reviewRepository.findByAuthorAndActiveTrue(
                targetUser,
                org.springframework.data.domain.PageRequest.of(0, 5, org.springframework.data.domain.Sort.by("createdAt").descending())
        ).stream().map(review -> ReviewListResponseDto.from(
                review,
                requesterEmail,
                false, // 좋아요 여부 로직 필요 시 추가
                0,     // 좋아요 카운트 로직 필요 시 추가
                review.getReviewImages().stream().map(img -> s3Service.publicUrl(img.getImageUrl())).toList()
        )).toList();

        // 4. 팔로우/팔로잉 리스트 (FollowService 활용)
        List<UserResponseDto> followers = followService.getFollowers(targetUserId);
        List<UserResponseDto> following = followService.getFollowing(targetUserId);

        // 5. 로그인한 사용자가 이 프로필 주인을 팔로우 중인지 확인
        boolean isFollowing = false;
        if (requesterEmail != null) {
            User requester = userRepository.findByEmail(requesterEmail).orElse(null);
            if (requester != null) {
                isFollowing = followService.isFollowing(requester.getId(), targetUserId);
            }
        }

        String profileUrl = (targetUser.getProfileImage() == null || targetUser.getProfileImage().isBlank())
                ? defaultProfileImage
                : s3Service.publicUrl(targetUser.getProfileImage());

        return UserProfileDetailResponseDto.builder()
                .userId(targetUser.getId())
                .nickname(targetUser.getNickname())
                .profileImageUrl(profileUrl)
                .level(targetUser.getLevel())
                .role(targetUser.getRole())
                .isFollowing(isFollowing)
                .boards(boards)
                .reviews(reviews)
                .followers(followers)
                .following(following)
                .build();
    }
}
