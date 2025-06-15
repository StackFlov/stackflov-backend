package com.stackflov.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "boards")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 게시글 작성자

    @Column(nullable = false, length = 100)
    private String title; // 게시글 제목

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content; // 게시글 내용

    @Column(nullable = false)
    private Integer category; // 게시글 카테고리

    @Column(nullable = false)
    @Builder.Default
    private Integer viewCount = 0; // 조회수

    @Column(nullable = false, columnDefinition = "BOOLEAN NOT NULL DEFAULT TRUE")
    private Boolean active = true;

    @CreationTimestamp
    private LocalDateTime createdAt; // 생성 일시

    @UpdateTimestamp
    private LocalDateTime updatedAt; // 마지막 수정 일시

    // 게시글 상태 업데이트 메서드
    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

    // 게시글 수정 메서드
    public void update(String title, String content, Integer category) {
        this.title = title;
        this.content = content;
        this.category = category;
    }

    // 조회수 증가 메서드
    public void incrementViewCount() {
        this.viewCount++;
    }

    // 편의 메서드 (Board와 Comment 양방향 연관관계 시)
    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    public void addComment(Comment comment) {
        this.comments.add(comment);
        comment.setBoard(this); // Comment 엔티티에 setBoard 메서드가 필요
    }

    public void removeComment(Comment comment) {
        this.comments.remove(comment);
        comment.setBoard(null);
    }

    // --- Board와 BoardImage 양방향 연관 관계 추가 시작 ---
    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default // Builder 사용 시 기본값으로 빈 리스트 초기화
    private List<BoardImage> boardImages = new ArrayList<>();

    // 편의 메서드 (양방향 관계 설정)
    public void addBoardImage(BoardImage boardImage) {
        this.boardImages.add(boardImage);
        boardImage.setBoard(this); // BoardImage 엔티티에 setBoard 메서드 호출
    }

    public void removeBoardImage(BoardImage boardImage) {
        this.boardImages.remove(boardImage);
        boardImage.setBoard(null);
    }
    // --- Board와 BoardImage 양방향 연관 관계 추가 끝 ---
}