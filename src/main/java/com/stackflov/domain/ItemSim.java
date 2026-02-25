package com.stackflov.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="item_sim")
public class ItemSim {

    @EmbeddedId
    private ItemSimId id;

    @Column(name="sim", nullable=false)
    private double sim;

    @Column(name="updated_at", nullable=false)
    private LocalDateTime updatedAt;

    protected ItemSim() {}

    public ItemSim(Long boardA, Long boardB, double sim, LocalDateTime updatedAt) {
        this.id = new ItemSimId(boardA, boardB);
        this.sim = sim;
        this.updatedAt = updatedAt;
    }

    public ItemSimId getId() { return id; }
    public double getSim() { return sim; }
}