package com.stackflov.repository;

import com.stackflov.domain.Location;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LocationRepository extends JpaRepository<Location, Long> {
    @Query("SELECT l FROM Location l " +
            "WHERE l.latitude BETWEEN :swLat AND :neLat " +
            "AND l.longitude BETWEEN :swLng AND :neLng")
    List<Location> findLocationsInBoundary(
            @Param("swLat") Double swLat, @Param("swLng") Double swLng,
            @Param("neLat") Double neLat, @Param("neLng") Double neLng
    );

    // 장소 이름으로 데이터 존재 여부 확인
    boolean existsByName(String name);

    // 주소로 데이터 존재 여부 확인
    boolean existsByAddress(String address);
}