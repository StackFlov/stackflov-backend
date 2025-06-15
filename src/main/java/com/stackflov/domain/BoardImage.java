package com.stackflov.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Entity
@Table(name = "board_images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class BoardImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Board와의 양방향 관계 설정을 위한 Setter (Board.addBoardImage에서 사용)
    // 게시판 연관관계 (N:1)
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board; // ✅ Board 엔티티와의 연관 관계 필드

    @Column(nullable = false)
    private String imageUrl;

    @Column
    private Integer sortOrder;

    @Column(nullable = false, columnDefinition = "BOOLEAN NOT NULL DEFAULT TRUE")
    private Boolean active = true; // 여기에 초기값 추가

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // 이미지 상태 업데이트 메서드 추가
    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

}