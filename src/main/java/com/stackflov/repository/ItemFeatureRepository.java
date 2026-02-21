package com.stackflov.repository; // ✅ 너희 repository 위치로

import com.stackflov.domain.ItemFeature;
import com.stackflov.domain.ItemFeatureId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemFeatureRepository extends JpaRepository<ItemFeature, ItemFeatureId> {
    void deleteById_BoardId(Long boardId); // ✅ Spring Data가 embeddedId 필드 접근 지원
}