package com.stackflov.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "likes", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "board_id"}) // 한 사용자가 같은 글에 중복으로 좋아요 누르는 것을 방지
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Like {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "like_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 좋아요를 누른 사용자

    // 게시글 좋아요일 때만 값이 있음 (아니면 NULL)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board board;

    // 리뷰 좋아요일 때만 값이 있음 (아니면 NULL)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    private Review review;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    public void deactivate() {
        this.active = false;
    }
    public void activate() { this.active = true; }
    public boolean isActive() { return active; }

    public void setActive(boolean active) { this.active = active; }
    public boolean isBoardLike() { return board != null && review == null; }
    public boolean isReviewLike() { return review != null && board == null; }
}
