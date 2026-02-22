package com.stackflov.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="user_feature_score")
public class UserFeatureScore {

    @EmbeddedId
    private UserFeatureScoreId id;

    @Column(name="score", nullable=false)
    private double score;

    @Column(name="updated_at", nullable=false)
    private LocalDateTime updatedAt;

    protected UserFeatureScore() {}

    public UserFeatureScore(Long userId, String featureType, String featureValue, double score, LocalDateTime updatedAt) {
        this.id = new UserFeatureScoreId(userId, featureType, featureValue);
        this.score = score;
        this.updatedAt = updatedAt;
    }

    public UserFeatureScoreId getId() { return id; }
    public double getScore() { return score; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}