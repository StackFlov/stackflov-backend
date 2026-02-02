package com.stackflov.service;

import com.stackflov.domain.NotificationType;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
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
    private final NotificationService notificationService;

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
    @Transactional
    public void register(SignupRequestDto signupRequestDto) {
        String email = signupRequestDto.getEmail();

        // 1. 이메일 인증 여부 확인 (Redis)
        String verified = redisService.get("EMAIL_VERIFIED:" + email);
        if (!"true".equals(verified)) {
            throw new IllegalArgumentException("이메일 인증이 완료되지 않았습니다.");
        }

        // 2. 프로필 이미지 처리
        String profileKey;
        try {
            String input = signupRequestDto.getProfileImage();
            profileKey = (input == null || input.isBlank())
                    ? s3Service.extractKey(defaultProfileImage)
                    : s3Service.extractKey(input);
        } catch (Exception e) {
            throw new IllegalArgumentException("이미지 처리에 실패했습니다.");
        }

        // 3. 유저 엔티티 생성 및 저장
        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(signupRequestDto.getPassword()))
                .nickname(signupRequestDto.getNickname())
                .profileImage(profileKey)
                .socialType(SocialType.NONE)
                .level(0)
                .exp(0)
                .role(Role.USER)
                .phoneNumber(signupRequestDto.getPhoneNumber())
                .address(signupRequestDto.getAddress())
                .addressDetail(signupRequestDto.getAddressDetail())
                .agreement(signupRequestDto.isAgreement())
                .build();

        userRepository.save(user);

        // 4. 가입 완료 후 인증 데이터 삭제
        redisService.delete("EMAIL_VERIFIED:" + email);
    }
    public UserResponseDto getUserByEmail(String email) {
        User user = userRepository.findByEmailAndActiveTrue(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 탈퇴한 사용자입니다."));
        String fullProfileUrl = s3Service.publicUrl(user.getProfileImage());

        UserResponseDto response = new UserResponseDto(user, fullProfileUrl);
        long followers = followService.getFollowers(user.getId()).size();
        long following = followService.getFollowing(user.getId()).size();
        response.setCounts(followers, following);

        return response;
    }

    @Transactional
    public void updateUser(String email, UserUpdateRequestDto dto) {
        User user = getValidUserByEmail(email);

        if (dto.getNickname() != null && StringUtils.hasText(dto.getNickname())) {
            user.updateNickname(dto.getNickname());
        }

        if (dto.getProfileImage() != null) {
            user.updateProfileImage(dto.getProfileImage());
        }

        if (dto.getPhoneNumber() != null) {
            user.updatePhoneNumber(dto.getPhoneNumber());
        }

        if (dto.getAddress() != null) {
            user.updateAddress(dto.getAddress());
        }

        if (dto.getAddressDetail() != null) {
            user.updateAddressDetail(dto.getAddressDetail());
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
                .exp(targetUser.getExp())
                .role(targetUser.getRole())
                .isFollowing(isFollowing)
                .boards(boards)
                .reviews(reviews)
                .followers(followers)
                .following(following)
                .build();
    }

    @Transactional
    public void grantExperience(Long userId, int amount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 1. 경험치 추가 및 레벨업 체크
        boolean isLevelUp = user.addExp(amount);

        // 2. 레벨업 했다면 알림 발송
        if (isLevelUp) {
            String levelName = getLevelName(user.getLevel());
            String message = "축하합니다! [" + levelName + "] 등급으로 레벨업하셨습니다! 🎉";

            // 기존에 만들어두신 notificationService 활용
            notificationService.notify(
                    user,
                    NotificationType.SYSTEM, // 또는 별도의 LEVEL_UP 타입 추가
                    message,
                    "/mypage" // 클릭 시 이동할 경로
            );
        }
    }
    private String getLevelName(int level) {
        String[] names = {"입문자", "먼지 먹는 하마", "편의점 미슐랭", "배달 앱 VVIP",
                "우리 동네 반장님", "빨래 건조대 수호자", "프로 자취 연금술사",
                "당근 온도 99도", "지박령", "자취방 만렙 교수", "StackFlov 성주"};
        if (level >= names.length) return names[names.length - 1];
        return names[level];
    }
}
