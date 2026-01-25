package com.stackflov.service;

import com.stackflov.domain.*;
import com.stackflov.dto.*;
import com.stackflov.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
    private final ReviewImageRepository reviewImageRepository;

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
                String imageUrl = s3Service.upload(image, "images");
                ReviewImage reviewImage = ReviewImage.builder()
                        .review(savedReview)
                        .imageUrl(imageUrl)
                        .build();
                savedReview.addReviewImage(reviewImage); // ✅ 연관관계 편의 메서드 사용
            }
        }
        user.addExp(10);
        userRepository.save(user);

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
    public void updateReview(Long reviewId,
                             ReviewUpdateRequestDto dto,
                             List<MultipartFile> images,
                             String userEmail) {

        // 1) 대상 리뷰 + 권한 확인
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));
        if (review.getAuthor() == null
                || review.getAuthor().getEmail() == null
                || !review.getAuthor().getEmail().equalsIgnoreCase(userEmail)) {
            throw new IllegalArgumentException("작성자만 수정할 수 있습니다.");
        }

        // 2) 스칼라 필드: null이 아닐 때만 반영 (부분수정)
        if (dto.getTitle()   != null) review.setTitle(dto.getTitle());
        if (dto.getAddress() != null) review.setAddress(dto.getAddress());
        if (dto.getContent() != null) review.setContent(dto.getContent());
        if (dto.getRating()  != null) {
            int clamped = Math.max(1, Math.min(5, dto.getRating()));
            review.setRating(clamped);
        }

        // 3) 이미지 삭제/전면 교체
        if (Boolean.TRUE.equals(dto.getReplaceAll())) {
            // 전면 교체: 기존 전부 삭제
            List<ReviewImage> all = reviewImageRepository.findAllByReviewId(reviewId);
            for (ReviewImage img : all) {
                s3Service.deleteByKey(img.getImageUrl());      // URL이어도 내부에서 key 추출해 삭제됨
                reviewImageRepository.delete(img);
            }
        } else if (dto.getDeleteImageIds() != null && !dto.getDeleteImageIds().isEmpty()) {
            // 일부 삭제: 해당 리뷰의 것만 안전하게 조회 후 삭제
            List<ReviewImage> toDelete =
                    reviewImageRepository.findAllByIdInAndReviewId(dto.getDeleteImageIds(), reviewId);
            for (ReviewImage img : toDelete) {
                s3Service.deleteByKey(img.getImageUrl());
                reviewImageRepository.delete(img);
            }
        }

        // 4) 새 이미지 추가
        if (images != null && !images.isEmpty()) {
            long order = reviewImageRepository.countByReviewId(reviewId);
            for (MultipartFile file : images) {
                if (file == null || file.isEmpty()) continue;
                String key = s3Service.upload(file, "images/"); // S3 key
                String url = s3Service.publicUrl(key);                      // CDN 도메인 우선
                reviewImageRepository.save(new ReviewImage(review, url)); // 엔티티에 맞게
            }
        }

        // JPA dirty checking으로 커밋 시 반영
    }
    @Transactional
    public void deleteReview(Long reviewId, String userEmail) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));

        if (review.getAuthor() == null
                || review.getAuthor().getEmail() == null
                || !review.getAuthor().getEmail().equalsIgnoreCase(userEmail)) {
            throw new IllegalArgumentException("작성자만 삭제할 수 있습니다.");
        }

        // 1) 리뷰에 연결된 이미지들 먼저 조회(추후 S3 삭제용)
        List<ReviewImage> images = reviewImageRepository.findAllByReviewId(reviewId);

        // 2) 리뷰/댓글 소프트 삭제
        review.deactivate();
        commentRepository.bulkDeactivateByReviewId(reviewId);

        // 3) S3에서 원본 삭제 (CDN URL이든 key든 deleteByKey가 알아서 key 추출)
        for (ReviewImage img : images) {
            String url = img.getImageUrl();
            if (url != null && !url.isBlank()) {
                try {
                    s3Service.deleteByKey(url);
                } catch (Exception e) {
                    // 이미지 하나 실패해도 전체 롤백하지 않도록 로깅만 하고 계속 진행
                    // 필요하면 @Slf4j 붙이고 log.warn 사용
                    System.out.println("S3 이미지 삭제 실패: " + url + " / " + e.getMessage());
                }
            }
        }

        // 4) 이미지 레코드 물리 삭제
        reviewImageRepository.deleteAll(images);
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

        // 작성자 프로필
        String rawProfile = review.getAuthor().getProfileImage();
        String authorProfileImageUrl = (rawProfile == null || rawProfile.isBlank())
                ? defaultProfileImage
                : s3Service.publicUrl(rawProfile);

        // 리뷰 이미지들
        List<String> imageUrls = review.getReviewImages().stream()
                .map(ReviewImage::getImageUrl)
                .filter(Objects::nonNull)
                .filter(s -> !s.isBlank())
                .map(this::toPublicUrl)      // ✅ 보정 + CDN URL
                .toList();

        long likeCount = likeRepository.countByReviewAndActiveTrue(review);
        boolean isLiked = (email != null) && userRepository.findByEmail(email)
                .map(u -> likeRepository.findByUserAndReviewAndActiveTrue(u, review).isPresent())
                .orElse(false);

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

    public List<ReviewListResponseDto> getReviewsByAuthor(User author, String requesterEmail) {
        // ReviewRepository에서 findByAuthorAndActiveTrue 사용
        return reviewRepository.findByAuthorAndActiveTrue(author, PageRequest.of(0, 10)).getContent()
                .stream()
                .map(r -> {
                    // 기존 getReviews의 변환 로직 재사용
                    return ReviewListResponseDto.from(r, requesterEmail, false, 0, Collections.emptyList());
                })
                .collect(Collectors.toList());
    }

    private String normalizeKey(String keyOrUrl) {
        String k = s3Service.extractKey(keyOrUrl);   // URL이면 path만, key면 그대로
        if (k.startsWith("review/"))  k = "images/" + k.substring("review/".length());
        if (k.startsWith("reviews/")) k = "images/" + k.substring("reviews/".length());
        return k.replaceFirst("^/+", "");
    }

    private String toPublicUrl(String keyOrUrl) {
        return s3Service.publicUrl(normalizeKey(keyOrUrl));
    }

}