package com.stackflov.repository;

import com.stackflov.domain.ChatRoom;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    @Query("""
      select r from ChatRoom r
      join r.participants p1
      join r.participants p2
      where p1.id = :u1 and p2.id = :u2
    """)
    Optional<ChatRoom> findDirectRoomBetween(@Param("u1") Long userId1, @Param("u2") Long userId2);
    @Query("SELECT r FROM ChatRoom r JOIN FETCH r.participants WHERE r.id = :roomId")
    Optional<ChatRoom> findByIdWithParticipants(@Param("roomId") Long roomId);
}