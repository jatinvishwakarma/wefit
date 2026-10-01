package com.wefit.moderationService.service;

import com.wefit.moderationService.dto.ReportRequest;
import com.wefit.moderationService.dto.ReportResponse;
import com.wefit.moderationService.dto.ReviewRequest;
import com.wefit.moderationService.model.ContentState;
import com.wefit.moderationService.model.Report;
import com.wefit.moderationService.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ModerationService {

    private final ReportRepository reportRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public ReportResponse createReport(ReportRequest request) {
        Report report = Report.builder()
                .reporterId(request.getReporterId())
                .contentId(request.getContentId())
                .contentType(request.getContentType())
                .reason(request.getReason())
                .description(request.getDescription())
                .state(ContentState.REPORTED)
                .build();
        
        Report savedReport = reportRepository.save(report);
        
        log.info("Report created: {}", savedReport.getId());
        
        // Check if content has been reported multiple times
        checkAutomatedFiltering(savedReport);
        
        return mapToResponse(savedReport);
    }
    
    public List<ReportResponse> getPendingReports() {
        return reportRepository.findByState(ContentState.REPORTED)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public ReportResponse reviewReport(Long reportId, ReviewRequest request) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));
                
        report.setState(request.getAction());
        report.setReviewerNotes(request.getReviewerNotes());
        
        Report savedReport = reportRepository.save(report);
        
        log.info("Report {} reviewed. Action: {}", savedReport.getId(), request.getAction());
        
        if (request.getAction() == ContentState.REMOVED || request.getAction() == ContentState.RESTORED) {
            publishModerationEvent(savedReport);
        }
        
        return mapToResponse(savedReport);
    }
    
    private void checkAutomatedFiltering(Report report) {
        List<Report> existingReports = reportRepository.findByContentIdAndContentType(report.getContentId(), report.getContentType());
        if (existingReports.size() >= 3) {
            log.info("Content {} auto-hidden due to multiple reports.", report.getContentId());
            // In a real system, publish event to hide immediately
            kafkaTemplate.send("moderation-events", String.format("{\"contentId\":\"%s\",\"contentType\":\"%s\",\"action\":\"AUTO_HIDE\"}", report.getContentId(), report.getContentType()));
        }
    }
    
    private void publishModerationEvent(Report report) {
        String action = report.getState() == ContentState.REMOVED ? "REMOVED" : "RESTORED";
        String event = String.format("{\"contentId\":\"%s\",\"contentType\":\"%s\",\"action\":\"%s\"}", 
                report.getContentId(), report.getContentType(), action);
        kafkaTemplate.send("moderation-events", event);
        log.info("Published moderation event: {}", event);
    }
    
    private ReportResponse mapToResponse(Report report) {
        return ReportResponse.builder()
                .id(report.getId())
                .reporterId(report.getReporterId())
                .contentId(report.getContentId())
                .contentType(report.getContentType())
                .reason(report.getReason())
                .description(report.getDescription())
                .state(report.getState())
                .createdAt(report.getCreatedAt())
                .build();
    }
}
