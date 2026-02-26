package com.stackflov.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class UserRecoId implements Serializable {

    @Column(name="user_id")
    private Long userId;

    @Column(name="board_id")
    private Long boardId;

    protected UserRecoId() {}

    public UserRecoId(Long userId, Long boardId) {
        this.userId = userId;
        this.boardId = boardId;
    }

    public Long getUserId() { return userId; }
    public Long getBoardId() { return boardId; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserRecoId that)) return false;
        return Objects.equals(userId, that.userId) && Objects.equals(boardId, that.boardId);
    }

    @Override public int hashCode() {
        return Objects.hash(userId, boardId);
    }
}