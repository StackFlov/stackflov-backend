package com.stackflov.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ItemSimId implements Serializable {

    @Column(name="board_a")
    private Long boardA;

    @Column(name="board_b")
    private Long boardB;

    protected ItemSimId() {}

    public ItemSimId(Long boardA, Long boardB) {
        this.boardA = boardA;
        this.boardB = boardB;
    }

    public Long getBoardA() { return boardA; }
    public Long getBoardB() { return boardB; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemSimId that)) return false;
        return Objects.equals(boardA, that.boardA) && Objects.equals(boardB, that.boardB);
    }

    @Override public int hashCode() {
        return Objects.hash(boardA, boardB);
    }
}