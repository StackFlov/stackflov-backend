// src/main/java/com/stackflov/reco/domain/UserEvent.java
package com.stackflov.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_event")
public class UserEvent {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userEventId;

    @Column(name="user_id", nullable=false)
    private Long userId;

    @Column(name="item_id", nullable=false)
    private Long itemId;

    @Enumerated(EnumType.STRING)
    @Column(name="event_type", nullable=false, length=32)
    private EventType eventType;

    @Column(name="value")
    private Integer value; // dwell_ms 등

    @Column(name="created_at", nullable=false)
    private LocalDateTime createdAt;

    protected UserEvent() {}

    public UserEvent(Long userId, Long itemId, EventType eventType, Integer value, LocalDateTime createdAt) {
        this.userId = userId;
        this.itemId = itemId;
        this.eventType = eventType;
        this.value = value;
        this.createdAt = createdAt;
    }

    public Long getUserId() { return userId; }
    public Long getItemId() { return itemId; }
    public EventType getEventType() { return eventType; }
    public Integer getValue() { return value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
