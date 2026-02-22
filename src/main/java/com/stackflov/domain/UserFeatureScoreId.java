package com.stackflov.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class UserFeatureScoreId implements Serializable {

    @Column(name="user_id")
    private Long userId;

    @Column(name="feature_type", length=32)
    private String featureType;

    @Column(name="feature_value", length=128)
    private String featureValue;

    protected UserFeatureScoreId() {}

    public UserFeatureScoreId(Long userId, String featureType, String featureValue) {
        this.userId = userId;
        this.featureType = featureType;
        this.featureValue = featureValue;
    }

    public Long getUserId() { return userId; }
    public String getFeatureType() { return featureType; }
    public String getFeatureValue() { return featureValue; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserFeatureScoreId that)) return false;
        return Objects.equals(userId, that.userId)
                && Objects.equals(featureType, that.featureType)
                && Objects.equals(featureValue, that.featureValue);
    }

    @Override public int hashCode() {
        return Objects.hash(userId, featureType, featureValue);
    }
}