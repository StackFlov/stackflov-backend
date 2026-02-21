package com.stackflov.service; // ✅

import com.stackflov.domain.FeatureType;
import com.stackflov.domain.ItemFeature;
import com.stackflov.repository.ItemFeatureRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ItemFeatureSyncService {

    private final ItemFeatureRepository itemFeatureRepository;

    public ItemFeatureSyncService(ItemFeatureRepository itemFeatureRepository) {
        this.itemFeatureRepository = itemFeatureRepository;
    }

    @Transactional
    public void syncBoardFeatures(Long boardId, int category, Long authorId, List<String> hashtags) {
        // 1) 기존 feature 삭제
        itemFeatureRepository.deleteById_BoardId(boardId);

        // 2) 새 feature 생성
        List<ItemFeature> features = new ArrayList<>();

        // CATEGORY: 카테고리는 int면 문자열로 저장(예: "3")
        features.add(new ItemFeature(boardId, FeatureType.CATEGORY, String.valueOf(category), 1.0));

        // AUTHOR: authorId도 문자열로 저장
        features.add(new ItemFeature(boardId, FeatureType.AUTHOR, String.valueOf(authorId), 1.0));

        // TAG: 해시태그들
        if (hashtags != null) {
            for (String tag : hashtags) {
                if (tag == null) continue;
                String cleaned = tag.trim();
                if (cleaned.isEmpty()) continue;
                features.add(new ItemFeature(boardId, FeatureType.TAG, cleaned, 1.0));
            }
        }

        itemFeatureRepository.saveAll(features);
    }
}