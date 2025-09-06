package com.stackflov.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notifications_receiver_created", columnList = "receiver_id, createdAt DESC")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;

    // 알림 수신자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    // 리스트에서 바로 보여줄 간단 메시지
    @Column(nullable = false, length = 200)
    private String message;

    // 프런트에서 이동할 링크(예: /boards/123)
    @Column(length = 300)
    private String link;

    @Builder.Default
    @Column(nullable = false)
    private boolean read = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public void markRead() {
        this.read = true;
    }
}
