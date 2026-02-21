package com.stackflov.domain; // ✅

import jakarta.persistence.*;

@Entity
@Table(name="item_feature")
public class ItemFeature {

    @EmbeddedId
    private ItemFeatureId id;

    @Column(name="weight", nullable=false)
    private double weight;

    protected ItemFeature() {}

    public ItemFeature(Long boardId, FeatureType type, String value, double weight) {
        this.id = new ItemFeatureId(boardId, type.name(), value);
        this.weight = weight;
    }

    public ItemFeatureId getId() { return id; }
    public double getWeight() { return weight; }
}