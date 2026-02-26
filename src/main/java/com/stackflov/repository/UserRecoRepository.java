package com.stackflov.repository;

import com.stackflov.domain.UserReco;
import com.stackflov.domain.UserRecoId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRecoRepository extends JpaRepository<UserReco, UserRecoId> {

    @Modifying
    @Query("delete from UserReco r where r.id.userId = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);

    @Query("""
        select r from UserReco r
        where r.id.userId = :userId
        order by r.score desc
    """)
    List<UserReco> findTopByUserId(@Param("userId") Long userId, Pageable pageable);
}