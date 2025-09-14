package com.stackflov.repository;

import com.stackflov.domain.AdminNote;
import com.stackflov.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdminNoteRepository extends JpaRepository<AdminNote, Long> {
    // 특정 사용자에 대한 모든 메모를 최신순으로 조회
    List<AdminNote> findByTargetUserOrderByCreatedAtDesc(User targetUser);
}