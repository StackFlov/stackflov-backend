package com.stackflov.repository;

import com.stackflov.domain.Attendance;
import com.stackflov.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    // 특정 유저의 가장 최근 출석 기록 하나를 가져옵니다.
    Optional<Attendance> findFirstByUserOrderByAttendanceDateDesc(User user);

    // 오늘 날짜 범위 내에 출석 기록이 있는지 확인합니다.
    boolean existsByUserAndAttendanceDateBetween(User user, LocalDateTime start, LocalDateTime end);
}