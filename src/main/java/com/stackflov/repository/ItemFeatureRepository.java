package com.stackflov.repository; // ✅ 너희 repository 위치로

import com.stackflov.domain.ItemFeature;
import com.stackflov.domain.ItemFeatureId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemFeatureRepository extends JpaRepository<ItemFeature, ItemFeatureId> {
    void deleteById_BoardId(Long boardId); // ✅ Spring Data가 embeddedId 필드 접근 지원

    @Query("""
    select f from ItemFeature f
    where f.id.boardId in :boardIds
""")
    List<ItemFeature> findByBoardIds(@Param("boardIds") List<Long> boardIds);
}