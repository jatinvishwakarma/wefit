package com.wefit.moderationService.controller;

import com.wefit.moderationService.dto.ReportRequest;
import com.wefit.moderationService.dto.ReportResponse;
import com.wefit.moderationService.dto.ReviewRequest;
import com.wefit.moderationService.service.ModerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/moderation")
@RequiredArgsConstructor
public class ModerationController {

    private final ModerationService moderationService;

    @PostMapping("/reports")
    public ResponseEntity<ReportResponse> createReport(@RequestBody ReportRequest request) {
        return ResponseEntity.ok(moderationService.createReport(request));
    }

    @GetMapping("/reports/pending")
    public ResponseEntity<List<ReportResponse>> getPendingReports() {
        return ResponseEntity.ok(moderationService.getPendingReports());
    }

    @PostMapping("/reports/{reportId}/review")
    public ResponseEntity<ReportResponse> reviewReport(
            @PathVariable Long reportId,
            @RequestBody ReviewRequest request) {
        return ResponseEntity.ok(moderationService.reviewReport(reportId, request));
    }
}
