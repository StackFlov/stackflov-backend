package com.stackflov.controller;

import com.stackflov.dto.ReportRequestDto;
import com.stackflov.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<String> createReport(
            @AuthenticationPrincipal String email,
            @RequestBody ReportRequestDto dto) {

        reportService.createReport(email, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body("신고가 정상적으로 접수되었습니다.");
    }
}
