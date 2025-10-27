package com.stackflov.service;

import com.stackflov.domain.*;
import com.stackflov.dto.*;
import com.stackflov.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MapService {

    private final ReviewRepository reviewRepository;
    private final S3Service s3Service;
    private final CommentRepository commentRepository;
    private final UserService userService;
    private final LikeRepository likeRepository;
    private final UserRepository userRepository;

    @Value("${app.defaults.profile-image}")
    private String defaultProfileImage;

    @Value("${app.cdn.domain}")   // 예: d3sutbt651osyh.cloudfront.net
    private String cdnDomain;

    // 특정 위치에 리뷰 작성
    @Transactional
    public Long createReview(ReviewRequestDto dto, String userEmail, List<MultipartFile> images) {
        User user = userService.getValidUserByEmail(userEmail);

        Review review = Review.builder()
                .author(user)
                .title(dto.getTitle())
                .address(dto.getAddress())     // ✅ 여기
                .content(dto.getContent())
                .rating(dto.getRating())
                .build();

        Review savedReview = reviewRepository.save(review);

        if (images != null && !images.isEmpty()) {
            for (MultipartFile image : images) {
                String imageUrl = s3Service.upload(image, "reviews");
                ReviewImage reviewImage = ReviewImage.builder()
                        .review(savedReview)
                        .imageUrl(imageUrl)
                        .build();
                savedReview.addReviewImage(reviewImage); // ✅ 연관관계 편의 메서드 사용
            }
        }
        return savedReview.getId();
    }


    public Page<ReviewListResponseDto> getReviews(Pageable pageable,
                                                 @org.springframework.lang.Nullable String requesterEmail) {
        Page<Review> page = reviewRepository.findByActiveTrue(pageable);

        List<Review> reviews = page.getContent();

        // ✅ 1. Java 8 수정: List.of() -> Collections.emptyList()
        if (reviews.isEmpty()) {
            return page.map(r -> ReviewListResponseDto.from(
                    r, requesterEmail, false, 0, Collections.emptyList() // 수정됨
            ));
        }

        List<Long> reviewIds = reviews.stream().map(Review::getId).toList();

        // 1) 좋아요 수 집계 (...생략...)
        Map<Long, Integer> likeCountMap = likeRepository.countActiveLikesByReviewIds(reviewIds)
                .stream()
                .collect(Collectors.toMap(
                        LikeRepository.ReviewLikeCount::getReviewId,
                        r -> r.getCnt().intValue()
                ));

        // 2) 내가 누른 리뷰 id 조회 (로그인 시에만)
        // ✅ 2. Java 8 수정: List.of -> Collections::emptyList
        List<Long> likedIds = userRepository.findByEmail(requesterEmail)
                .map(u -> likeRepository.findLikedReviewIds(u.getId(), reviewIds))
                .orElseGet(Collections::emptyList); // 수정됨

        Set<Long> likedSet = new HashSet<>(likedIds);

        final Map<Long, Integer> likeCountMapFinal = likeCountMap;
        final Set<Long> likedSetFinal = likedSet;

        return page.map(r -> {
            // ✅ 3. Java 8 수정: List.of() -> Collections.emptyList()
            List<String> imageUrls = r.getReviewImages() == null ? Collections.emptyList() // 수정됨
                    : r.getReviewImages().stream()
                    .map(img -> s3Service.publicUrl(img.getImageUrl()))
                    .toList();

            return ReviewListResponseDto.from(
                    r,
                    requesterEmail,
                    likedSetFinal.contains(r.getId()),
                    likeCountMapFinal.getOrDefault(r.getId(), 0),
                    imageUrls
            );
        });
    }
    @Transactional
    public void updateReview(Long reviewId, ReviewRequestDto dto, String userEmail) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));

        if (!review.getAuthor().getEmail().equals(userEmail)) {
            throw new IllegalArgumentException("작성자만 수정할 수 있습니다.");
        }

        review.update(dto.getTitle(), dto.getAddress(), dto.getContent(), dto.getRating());
    }
    @Transactional
    public void deleteReview(Long reviewId, String userEmail) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));

        if (!review.getAuthor().getEmail().equals(userEmail)) {
            throw new IllegalArgumentException("작성자만 삭제할 수 있습니다.");
        }

        review.deactivate(); // 👈 delete -> deactivate 로 변경

        commentRepository.bulkDeactivateByReviewId(reviewId);
    }
    @Transactional
    public void deactivateReviewByAdmin(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));

        // 리뷰 비활성화
        review.deactivate();

        // 해당 리뷰에 달린 모든 댓글도 함께 비활성화
        commentRepository.findByReviewIdAndActiveTrue(reviewId).forEach(Comment::deactivate);
    }

    private ReviewSimpleResponseDto toSimpleDto(Review r) {
        return new ReviewSimpleResponseDto(
                r.getId(),
                r.getAddress(),
                r.getContent(),
                r.getRating(),
                r.getCreatedAt().toLocalDate()
        );
    }

    @Transactional(readOnly = true)
    public ReviewDetailResponseDto getReviewDetail(Long reviewId, String email) {
        Review review = reviewRepository.findByIdAndActiveTrue(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰가 없거나 비활성화되었습니다."));

        // 작성자 프로필 이미지
        String rawProfile = review.getAuthor().getProfileImage();
        String authorProfileImageUrl = (rawProfile == null || rawProfile.trim().isEmpty())
                ? defaultProfileImage
                : s3Service.publicUrl(rawProfile);

        // 리뷰 이미지 URL 리스트 (정렬: sortOrder 가 있을 때 / 없으면 id 로)
        List<String> imageUrls = review.getReviewImages().stream()
                .map(ReviewImage::getImageUrl)
                .filter(Objects::nonNull)
                .filter(s -> !s.isBlank())
                .map(this::toPublicUrl)              // key면 CDN URL로 변환
                .toList();         // ✅ JDK 8/11 호환

        long likeCount = likeRepository.countByReviewAndActiveTrue(review);
        boolean isLiked = false;
        if (email != null) {
            userRepository.findByEmail(email).ifPresent(u -> {
                // 필요 시 필드에 담거나, 로컬 변수 사용은 람다에서 불가하니 아래처럼 한 번 더 조회
            });
            isLiked = userRepository.findByEmail(email)
                    .map(u -> likeRepository.findByUserAndReviewAndActiveTrue(u, review).isPresent())
                    .orElse(false);
        }

        return ReviewDetailResponseDto.builder()
                .id(review.getId())
                .title(review.getTitle())
                .content(review.getContent())
                .address(review.getAddress())
                .rating(review.getRating())
                .authorId(review.getAuthor().getId())
                .authorEmail(review.getAuthor().getEmail())
                .authorNickname(review.getAuthor().getNickname())
                .authorProfileImageUrl(authorProfileImageUrl)
                .imageUrls(imageUrls)
                .likeCount(likeCount)
                .isLiked(isLiked)
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }


    private String toPublicUrl(String keyOrUrl) {
        if (keyOrUrl == null || keyOrUrl.isBlank()) return "";
        if (keyOrUrl.startsWith("http://") || keyOrUrl.startsWith("https://")) {
            return keyOrUrl; // 이미 절대 URL
        }
        // S3 key → CDN URL
        return "https://" + cdnDomain + "/" + keyOrUrl.replaceFirst("^/+", "");
    }
}