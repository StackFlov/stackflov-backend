package com.stackflov.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "bookmarks", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "board_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Bookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bookmark_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 북마크를 한 사용자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board; // 북마크된 게시판

    @CreationTimestamp
    private LocalDateTime createdAt; // 북마크 생성 일시

    // --- 아래 필드를 추가합니다 ---
    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    // === 비활성화를 위한 비즈니스 메서드 추가 ===
    public void deactivate() {
        this.active = false;
    }
}