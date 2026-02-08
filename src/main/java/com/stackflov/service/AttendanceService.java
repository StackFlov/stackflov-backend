package com.stackflov.service;

import com.stackflov.domain.Attendance;
import com.stackflov.domain.NotificationType;
import com.stackflov.domain.User;
import com.stackflov.repository.AttendanceRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final NotificationService notificationService;

    @Transactional
    public String checkIn(User user) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = now.toLocalDate().atTime(23, 59, 59);

        // 1. 중복 출석 체크
        if (attendanceRepository.existsByUserAndAttendanceDateBetween(user, startOfDay, endOfDay)) {
            throw new IllegalStateException("오늘은 이미 출석 체크를 완료했습니다!");
        }

        // 2. 연속 출석 일수(Streak) 계산
        int continuousDays = 1;
        Optional<Attendance> lastAttendance = attendanceRepository.findFirstByUserOrderByAttendanceDateDesc(user);

        if (lastAttendance.isPresent()) {
            LocalDateTime lastDate = lastAttendance.get().getAttendanceDate();
            // 마지막 출석이 어제인 경우 연속 일수 증가
            if (lastDate.toLocalDate().equals(now.toLocalDate().minusDays(1))) {
                continuousDays = lastAttendance.get().getContinuousDays() + 1;
            }
        }

        // 3. 경험치(XP) 보상 계산 (Base: 10XP)
        int earnedExp = 10;
        String bonusMsg = "";

        if (continuousDays == 3) {
            earnedExp += 20;
            bonusMsg = " (3일 연속 보너스 +20XP!)";
        } else if (continuousDays == 7) {
            earnedExp += 50;
            bonusMsg = " (7일 연속 보너스 +50XP!)";
            // 7일 이후에는 다시 1일차로 리셋하거나 정책에 따라 변경 가능
        }

        // 4. 유저 경험치 반영 및 저장
        user.addExp(earnedExp);

        Attendance attendance = new Attendance();
        attendance.setUser(user);
        attendance.setAttendanceDate(now);
        attendance.setContinuousDays(continuousDays);
        attendance.setEarnedExp(earnedExp);
        attendanceRepository.save(attendance);

        // 5. 알림 전송
        notificationService.notify(
                user,
                NotificationType.SYSTEM,
                continuousDays + "일 연속 출석 성공! " + earnedExp + "XP를 획득했습니다." + bonusMsg,
                "/mypage"
        );

        return continuousDays + "일 연속 출석! " + earnedExp + "XP 획득";
    }
}
