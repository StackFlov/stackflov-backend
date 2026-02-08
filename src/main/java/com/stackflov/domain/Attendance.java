package com.stackflov.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long attendanceId; // ✅ 그냥 id 대신 attendanceId로 변경

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // DB 외래키 이름은 관례에 따라 user_id로 설정
    private User user;

    private LocalDateTime attendanceDate; // 출석 시간
    private int continuousDays; // 연속 출석 일수

    // 경험치 지급 여부나 획득 경험치 양을 기록하고 싶다면 추가 가능
    private int earnedExp;
}
