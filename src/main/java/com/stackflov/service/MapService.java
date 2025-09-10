package com.stackflov.service;

import com.stackflov.domain.*;
import com.stackflov.dto.*;
import com.stackflov.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MapService {

    private final LocationRepository locationRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final S3Service s3Service;

    // 새로운 위치(화살표) 생성
    @Transactional
    public Long createLocation(LocationDto dto) {
        Location location = Location.builder()
                .address(dto.getAddress())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .build();
        return locationRepository.save(location).getId();
    }

    // 특정 위치에 리뷰 작성
    @Transactional
    public Long createReview(Long locationId, ReviewRequestDto dto, String userEmail, List<MultipartFile> images) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new IllegalArgumentException("위치를 찾을 수 없습니다."));

        Review review = Review.builder()
                .location(location)
                .author(user)
                .title(dto.getTitle())
                .content(dto.getContent())
                .rating(dto.getRating())
                .build();

        Review savedReview = reviewRepository.save(review);

        // 이미지 파일이 있다면 S3에 업로드하고 ReviewImage로 저장
        if (images != null && !images.isEmpty()) {
            for (MultipartFile image : images) {
                String imageUrl = s3Service.upload(image, "reviews"); // S3의 'reviews' 폴더에 저장
                ReviewImage reviewImage = ReviewImage.builder()
                        .review(savedReview)
                        .imageUrl(imageUrl)
                        .build();
                // Review 엔티티에 ReviewImage를 추가 (양방향 연관관계 설정)
                savedReview.getReviewImages().add(reviewImage);
            }
        }
        return savedReview.getId();
    }

    // 특정 위치의 모든 리뷰 조회
    @Transactional(readOnly = true)
    public List<LocationDto> getLocationsInMap(MapBoundaryDto dto) {
        return locationRepository.findLocationsInBoundary(
                dto.getSwLat(), dto.getSwLng(),
                dto.getNeLat(), dto.getNeLng()
        ).stream().map(LocationDto::new).collect(Collectors.toList());
    }

    // (지도 영역 내 위치 조회 기능은 여기에 추가됩니다)
    public List<ReviewResponseDto> getReviews(Long locationId) {
        List<Review> reviews = reviewRepository.findByLocationId(locationId);
        return reviews.stream()
                .map(ReviewResponseDto::new)
                .collect(Collectors.toList());
    }
    @Transactional
    public void updateReview(Long reviewId, ReviewRequestDto dto, String userEmail) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));

        if (!review.getAuthor().getEmail().equals(userEmail)) {
            throw new IllegalArgumentException("작성자만 수정할 수 있습니다.");
        }

        review.update(dto.getTitle(), dto.getContent(), dto.getRating());
    }
    @Transactional
    public void deleteReview(Long reviewId, String userEmail) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));

        if (!review.getAuthor().getEmail().equals(userEmail)) {
            throw new IllegalArgumentException("작성자만 삭제할 수 있습니다.");
        }

        review.deactivate(); // 👈 delete -> deactivate 로 변경
    }
}