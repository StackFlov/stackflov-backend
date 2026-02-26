package com.stackflov.service;

import com.stackflov.repository.UserEventRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class RecoBatchJob {

    private final UserEventRepository userEventRepository;
    private final UserProfileService userProfileService;         // Step 5
    private final ItemSimilarityService itemSimilarityService;   // Step 6
    private final RecommendationService recommendationService;   // Step 7

    public RecoBatchJob(
            UserEventRepository userEventRepository,
            UserProfileService userProfileService,
            ItemSimilarityService itemSimilarityService,
            RecommendationService recommendationService
    ) {
        this.userEventRepository = userEventRepository;
        this.userProfileService = userProfileService;
        this.itemSimilarityService = itemSimilarityService;
        this.recommendationService = recommendationService;
    }

    // 30분마다(원하는 주기로 변경)
    @Scheduled(cron = "0 */30 * * * *")
    @Transactional
    public void run() {
        // 1) item_sim 갱신
        itemSimilarityService.rebuildItemSimForActiveUsers();

        // 2) 활성 유저에 대해 프로필 + 추천 캐시 갱신
        List<Long> activeUsers = userEventRepository.findActiveUserIds(LocalDateTime.now().minusDays(7));
        for (Long userId : activeUsers) {
            userProfileService.rebuildUserProfile(userId);
            recommendationService.rebuildUserReco(userId);
        }
    }
}