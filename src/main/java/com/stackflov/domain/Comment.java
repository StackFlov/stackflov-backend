package com.stackflov.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id") // nullable = false 제거
    private Board board; // 댓글이 달린 게시판

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id") // 리뷰 댓글일 경우 여기에 값이 들어감
    private Review review;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;  // 댓글 작성한 사용자

    @Column(nullable = false)
    private String content;  // 댓글 내용

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    // === 비활성화를 위한 비즈니스 메서드 추가 ===
    public void deactivate() {
        this.active = false;
    }

    // 댓글 내용을 수정하는 메서드
    public void updateContent(String content) {
        this.content = content;
    }
}