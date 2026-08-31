package com.lendmate.notificationservice.service.impl;

import com.lendmate.notificationservice.model.Enum.FailedEventStatus;
import com.lendmate.notificationservice.model.FailedEvent;
import com.lendmate.notificationservice.repository.FailedEventRepository;
import com.lendmate.notificationservice.service.FailedEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class FailedEventServiceImpl implements FailedEventService {
    private final FailedEventRepository repository;

    @Override
    public void recordFailure(UUID eventId, Long orderId, String originalTopic, String payload, String exceptionMessage) {
        FailedEvent failedEvent =  FailedEvent.builder()
                .eventId(eventId)
                .orderId(orderId)
                .originalTopic(originalTopic)
                .payload(payload)
                .exceptionMessage(exceptionMessage)
                .build();

        repository.save(failedEvent);
        log.info("Recorded failed event: eventId={}, orderId={}", eventId, orderId);
    }

    @Override
    public List<FailedEvent> findPending() {
        return repository.findByStatus(FailedEventStatus.PENDING);
    }

    @Override
    public long countAttempts(UUID eventId) {
        return repository.countByEventId(eventId);
    }

    @Override
    @Transactional
    public void markAsRetried(Long id) {
        repository.findById(id).ifPresent(f->{
            f.setStatus(FailedEventStatus.RETRIED);
            f.setRetriedAt(Instant.now());
        });
    }
}