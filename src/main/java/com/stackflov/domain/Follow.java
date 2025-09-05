package com.stackflov.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "follows", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"follower_id", "followed_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Follow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "follow_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", nullable = false)
    private User follower;  // 팔로우하는 사용자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "followed_id", nullable = false)
    private User followed;  // 팔로우 당하는 사용자

    @CreationTimestamp
    private LocalDateTime createdAt;  // 팔로우 관계 생성 시각

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    public void setActive(boolean active) { this.active = active; }

    // === 비활성화를 위한 비즈니스 메서드 추가 ===
    public void deactivate() {
        this.active = false;
    }
    public void activate() { this.active = true; }
    public boolean isActive() { return active; }
}