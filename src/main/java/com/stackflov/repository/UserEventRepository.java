package com.stackflov.repository;

import com.stackflov.domain.UserEvent;
import com.stackflov.domain.EventType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface UserEventRepository extends JpaRepository<UserEvent, Long> {

    // 최근 N일 내 유저가 남긴 이벤트들
    @Query("""
        select e from UserEvent e
        where e.userId = :userId
          and e.createdAt >= :since
        order by e.createdAt desc
    """)
    List<UserEvent> findRecentEvents(
            @Param("userId") Long userId,
            @Param("since") LocalDateTime since
    );

    // 최근 N일 내 "활성 유저" 목록(배치 대상)
    @Query("""
        select distinct e.userId from UserEvent e
        where e.createdAt >= :since
    """)
    List<Long> findActiveUserIds(@Param("since") LocalDateTime since);

    // 최근 N일 내 유저의 positive 아이템(board) 목록(최신순)
    @Query("""
        select e.boardId from UserEvent e
        where e.userId = :userId
          and e.createdAt >= :since
          and e.eventType in :positiveTypes
        group by e.boardId
        order by max(e.createdAt) desc
    """)
    List<Long> findRecentPositiveBoardIds(
            @Param("userId") Long userId,
            @Param("since") LocalDateTime since,
            @Param("positiveTypes") List<EventType> positiveTypes
    );
}
