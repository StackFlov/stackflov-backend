package com.stackflov.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "mentions")
public class Mention {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 멘션을 한 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentioner_id", nullable = false)
    private User mentioner;

    // 멘션된 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentioned_id", nullable = false)
    private User mentioned;

    // 멘션이 포함된 게시글 (nullable)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board board;

    // 멘션이 포함된 댓글 (nullable)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private Comment comment;
}