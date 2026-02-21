package com.stackflov.controller; // ✅ 너희 프로젝트에 맞게

import com.stackflov.domain.EventType;          // ✅ 경로 맞추기
import com.stackflov.security.CurrentUserId;
import com.stackflov.service.EventIngestService; // ✅ 경로 맞추기
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
public class UserEventController {

    private final EventIngestService eventIngestService;

    public UserEventController(EventIngestService eventIngestService) {
        this.eventIngestService = eventIngestService;
    }

    // 요청 바디: { "boardId": 123, "type": "VIEW", "value": null }
    public record EventRequest(Long boardId, EventType type, Integer value) {}

    @PostMapping
    public ResponseEntity<Void> ingest(@RequestBody EventRequest req) {
        Long userId = CurrentUserId.get(); // ✅ Step 3-2에서 구현
        eventIngestService.ingest(userId, req.boardId(), req.type(), req.value());
        return ResponseEntity.ok().build();
    }
}