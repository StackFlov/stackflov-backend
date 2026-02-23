package com.stackflov.service;

import com.stackflov.domain.*;
import com.stackflov.repository.ItemFeatureRepository;
import com.stackflov.repository.UserEventRepository;
import com.stackflov.repository.UserFeatureScoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserProfileService {

    private final UserEventRepository userEventRepository;
    private final ItemFeatureRepository itemFeatureRepository;
    private final UserFeatureScoreRepository userFeatureScoreRepository;

    // 튜닝값(원하면 나중에 yml로 뺄 수 있음)
    private final int profileDays = 30;
    private final double decayLambda = 0.10; // exp(-lambda * daysAgo)

    public UserProfileService(
            UserEventRepository userEventRepository,
            ItemFeatureRepository itemFeatureRepository,
            UserFeatureScoreRepository userFeatureScoreRepository
    ) {
        this.userEventRepository = userEventRepository;
        this.itemFeatureRepository = itemFeatureRepository;
        this.userFeatureScoreRepository = userFeatureScoreRepository;
    }

    @Transactional
    public void rebuildUserProfile(Long userId) {
        LocalDateTime since = LocalDateTime.now().minusDays(profileDays);

        List<UserEvent> events = userEventRepository.findRecentEvents(userId, since);
        if (events.isEmpty()) {
            userFeatureScoreRepository.deleteAllByUserId(userId);
            return;
        }

        // 이벤트에 등장한 boardId들
        List<Long> boardIds = events.stream()
                .map(UserEvent::getBoardId)
                .distinct()
                .toList();

        // boardId -> features
        Map<Long, List<ItemFeature>> featuresByBoard =
                itemFeatureRepository.findByBoardIds(boardIds).stream()
                        .collect(Collectors.groupingBy(f -> f.getId().getBoardId()));

        // feature 점수 누적 (key = "TYPE|VALUE")
        Map<String, Double> scoreMap = new HashMap<>();
        LocalDate today = LocalDate.now();

        for (UserEvent e : events) {
            double wEvent = eventWeight(e);
            double decay = timeDecay(today, e.getCreatedAt().toLocalDate());

            List<ItemFeature> feats = featuresByBoard.getOrDefault(e.getBoardId(), List.of());
            for (ItemFeature f : feats) {
                String key = f.getId().getFeatureType() + "|" + f.getId().getFeatureValue();
                double add = wEvent * decay * f.getWeight();
                scoreMap.merge(key, add, Double::sum);
            }
        }

        // 기존 프로필 삭제 후 재생성(단순/안전)
        userFeatureScoreRepository.deleteAllByUserId(userId);

        LocalDateTime now = LocalDateTime.now();
        List<UserFeatureScore> rows = new ArrayList<>(scoreMap.size());
        for (Map.Entry<String, Double> en : scoreMap.entrySet()) {
            if (en.getValue() <= 0) continue;
            String[] parts = en.getKey().split("\\|", 2);
            rows.add(new UserFeatureScore(userId, parts[0], parts[1], en.getValue(), now));
        }
        userFeatureScoreRepository.saveAll(rows);
    }

    private double eventWeight(UserEvent e) {
        return switch (e.getEventType()) {
            case VIEW -> 1.0;
            case CLICK -> 2.0;
            case LIKE -> 5.0;
            case BOOKMARK -> 6.0;
            case DWELL -> {
                int ms = Optional.ofNullable(e.getValue()).orElse(0);
                yield Math.min(ms / 3000.0, 3.0); // 0~3
            }
        };
    }

    private double timeDecay(LocalDate today, LocalDate eventDate) {
        long daysAgo = Math.max(0, ChronoUnit.DAYS.between(eventDate, today));
        return Math.exp(-decayLambda * daysAgo);
    }
}