package com.stackflov.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="user_reco")
public class UserReco {

    @EmbeddedId
    private UserRecoId id;

    @Column(name="score", nullable=false)
    private double score;

    @Column(name="reason", length=64)
    private String reason; // "hybrid"

    @Column(name="generated_at", nullable=false)
    private LocalDateTime generatedAt;

    protected UserReco() {}

    public UserReco(Long userId, Long boardId, double score, String reason, LocalDateTime generatedAt) {
        this.id = new UserRecoId(userId, boardId);
        this.score = score;
        this.reason = reason;
        this.generatedAt = generatedAt;
    }

    public UserRecoId getId() { return id; }
    public double getScore() { return score; }
}