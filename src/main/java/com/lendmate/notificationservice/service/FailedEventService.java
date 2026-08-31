package com.lendmate.notificationservice.service;

import com.lendmate.notificationservice.model.FailedEvent;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FailedEventService {
    void recordFailure(UUID eventId, Long orderId, String originalTopic, String payload, String exceptionMessage);
    List<FailedEvent> findPending();
    long countAttempts(UUID eventId);
    void markAsRetried(Long id);
}