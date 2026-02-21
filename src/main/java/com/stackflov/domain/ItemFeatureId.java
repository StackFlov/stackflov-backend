package com.stackflov.domain; // ✅

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ItemFeatureId implements Serializable {

    @Column(name="board_id")
    private Long boardId;

    @Column(name="feature_type", length=32)
    private String featureType;

    @Column(name="feature_value", length=128)
    private String featureValue;

    protected ItemFeatureId() {}

    public ItemFeatureId(Long boardId, String featureType, String featureValue) {
        this.boardId = boardId;
        this.featureType = featureType;
        this.featureValue = featureValue;
    }

    public Long getBoardId() { return boardId; }
    public String getFeatureType() { return featureType; }
    public String getFeatureValue() { return featureValue; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemFeatureId that)) return false;
        return Objects.equals(boardId, that.boardId)
                && Objects.equals(featureType, that.featureType)
                && Objects.equals(featureValue, that.featureValue);
    }

    @Override public int hashCode() {
        return Objects.hash(boardId, featureType, featureValue);
    }
}