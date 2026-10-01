package com.wefit.aiService.repositories;

import com.wefit.aiService.entities.AnalyticsReport;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnalyticsReportRepository extends MongoRepository<AnalyticsReport, String> {
    List<AnalyticsReport> findByUserIdOrderByCreatedAtDesc(Long userId);
}
