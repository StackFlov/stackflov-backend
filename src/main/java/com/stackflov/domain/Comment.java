package com.stackflov.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.ColumnDefault; // ColumnDefault 임포트 추가

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

    @ManyToOne(fetch = FetchType.LAZY) // EAGER 대신 LAZY로 변경 권장 (성능)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;  // 댓글이 달린 게시판

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;  // 댓글 작성한 사용자

    @Column(nullable = false, length = 100)
    private String title;  // 댓글 제목

    @Column(nullable = false)
    private String content;  // 댓글 내용

    @Column(nullable = false, columnDefinition = "BOOLEAN NOT NULL DEFAULT TRUE")
    private Boolean active = true; // 여기에 초기값 추가

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // 댓글 제목을 수정하는 메서드
    public void updateTitle(String title) {
        this.title = title;
    }

    // 댓글 내용을 수정하는 메서드
    public void updateContent(String content) {
        this.content = content;
    }

    // 댓글 상태 업데이트 메서드 추가
    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

    // Board와의 양방향 관계 설정을 위한 Setter (Board.addComment에서 사용)
    public void setBoard(Board board) {
        this.board = board;
    }
}