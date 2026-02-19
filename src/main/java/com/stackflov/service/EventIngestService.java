package com.stackflov.service;

import com.stackflov.domain.EventType;
import com.stackflov.domain.UserEvent;
import com.stackflov.repository.UserEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class EventIngestService {

    private final UserEventRepository userEventRepository;

    public EventIngestService(UserEventRepository userEventRepository) {
        this.userEventRepository = userEventRepository;
    }

    @Transactional
    public void ingest(Long userId, Long boardId, EventType type, Integer value) {
        UserEvent event = new UserEvent(
                userId,
                boardId,
                type,
                value,
                LocalDateTime.now()
        );
        userEventRepository.save(event);
    }
}
