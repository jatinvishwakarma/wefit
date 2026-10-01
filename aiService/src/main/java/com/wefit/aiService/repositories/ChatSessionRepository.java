package com.wefit.aiService.repositories;

import com.wefit.aiService.entities.ChatSession;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatSessionRepository extends MongoRepository<ChatSession, String> {
    Optional<ChatSession> findByUserId(Long userId);
}
