package com.wefit.aiService.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.wefit.aiService.entities.Activity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityMessageListener {
    private final ActivityAiService activityAiService;

    @KafkaListener(topics = "${kafka.topic.name}", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(Activity activity) {
        log.info("Received Activity for processing: {}", activity.getId());
        activityAiService.generateRecommendation(activity);
    }

}
