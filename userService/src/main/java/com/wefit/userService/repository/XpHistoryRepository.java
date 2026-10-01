package com.wefit.userService.repository;

import com.wefit.userService.entities.XpHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface XpHistoryRepository extends JpaRepository<XpHistory, Long> {
    List<XpHistory> findByUserIdOrderByCreatedDateTimeDesc(Long userId);
}
