package com.stackflov.repository;

import com.stackflov.domain.ItemSim;
import com.stackflov.domain.ItemSimId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemSimRepository extends JpaRepository<ItemSim, ItemSimId> {

    @Modifying
    @Query("delete from ItemSim s where s.id.boardA = :boardA")
    void deleteByBoardA(@Param("boardA") Long boardA);

    @Query("""
        select s from ItemSim s
        where s.id.boardA = :boardA
        order by s.sim desc
    """)
    List<ItemSim> findTopSimilar(@Param("boardA") Long boardA, Pageable pageable);
}