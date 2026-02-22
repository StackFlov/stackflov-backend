package com.stackflov.repository;

import com.stackflov.domain.UserFeatureScore;
import com.stackflov.domain.UserFeatureScoreId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserFeatureScoreRepository extends JpaRepository<UserFeatureScore, UserFeatureScoreId> {

    @Modifying
    @Query("delete from UserFeatureScore u where u.id.userId = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);

    @Query("""
        select u from UserFeatureScore u
        where u.id.userId = :userId
        order by u.score desc
    """)
    List<UserFeatureScore> findTopByUserId(@Param("userId") Long userId, Pageable pageable);
}