package com.stackflov.service;

import com.stackflov.domain.*;
import com.stackflov.dto.*;
import com.stackflov.repository.*;
import io.micrometer.common.lang.Nullable;
import lombok.RequiredArgsConstructor;
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
    private final LikeRepository likeRepository;   // ✅
    private final UserRepository userRepository;

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


    public Page<ReviewListResponseDto> getReview(Pageable pageable,
                                                 @org.springframework.lang.Nullable String requesterEmail) {
        Page<Review> page = reviewRepository.findByActiveTrue(pageable);

        List<Review> reviews = page.getContent();
        if (reviews.isEmpty()) return page.map(r -> ReviewListResponseDto.from(r, requesterEmail, false, 0));

        List<Long> reviewIds = reviews.stream().map(Review::getId).toList();

        // 1) 좋아요 수 집계
        Map<Long, Integer> likeCountMap = likeRepository.countActiveLikesByReviewIds(reviewIds)
                .stream()
                .collect(Collectors.toMap(
                        LikeRepository.ReviewLikeCount::getReviewId,
                        r -> r.getCnt().intValue()
                ));

        // 2) 내가 누른 리뷰 id 조회 (로그인 시에만)
        Set<Long> likedSet = Collections.emptySet();
        if (requesterEmail != null) {
            userRepository.findByEmail(requesterEmail).ifPresent(user -> {
                List<Long> likedIds = likeRepository.findLikedReviewIds(user.getId(), reviewIds);
                // capture를 위해 로컬 최종 변수 사용 대신 배열/holder 쓰거나 아래처럼 처리
                // 외부 변수 재할당 피하려면 람다 밖에서 처리:
            });
            // 람다 캡처 깔끔 버전:
            List<Long> likedIds = userRepository.findByEmail(requesterEmail)
                    .map(u -> likeRepository.findLikedReviewIds(u.getId(), reviewIds))
                    .orElseGet(List::of);
            likedSet = new HashSet<>(likedIds);
        }

        final Map<Long, Integer> likeCountMapFinal = likeCountMap;
        final Set<Long> likedSetFinal = likedSet;

        return page.map(r -> ReviewListResponseDto.from(
                r,
                requesterEmail,
                likedSetFinal.contains(r.getId()),                     // liked
                likeCountMapFinal.getOrDefault(r.getId(), 0)           // likeCount
        ));
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
}