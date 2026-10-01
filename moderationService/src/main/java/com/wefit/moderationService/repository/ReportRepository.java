package com.wefit.moderationService.repository;

import com.wefit.moderationService.model.ContentState;
import com.wefit.moderationService.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByState(ContentState state);
    List<Report> findByContentIdAndContentType(String contentId, String contentType);
}
