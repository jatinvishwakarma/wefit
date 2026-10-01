package com.wefit.aiService.controller;

import com.wefit.aiService.dto.AnalyticsReportRequest;
import com.wefit.aiService.dto.AnalyticsReportResponse;
import com.wefit.aiService.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ai/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @PostMapping("/{userId}/generate")
    public ResponseEntity<AnalyticsReportResponse> generateReport(
            @PathVariable Long userId,
            @RequestBody AnalyticsReportRequest request) {
        return ResponseEntity.ok(analyticsService.generateAnalyticsReport(userId, request));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<AnalyticsReportResponse>> getUserReports(@PathVariable Long userId) {
        return ResponseEntity.ok(analyticsService.getUserReports(userId));
    }
}
