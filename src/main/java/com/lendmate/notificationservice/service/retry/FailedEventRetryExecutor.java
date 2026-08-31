package com.lendmate.notificationservice.service.retry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lendmate.notificationservice.kafka.OrderEvent;
import com.lendmate.notificationservice.model.FailedEvent;
import com.lendmate.notificationservice.service.FailedEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class FailedEventRetryExecutor {
    private final FailedEventService failedEventService;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final int MAX_AUTO_RETRY_ATTEMPTS = 5;

    public int reprocessAllPending() {
        List<FailedEvent> pendingEvents = failedEventService.findPending();
        if (pendingEvents.isEmpty()) {
            log.info("No pending failed events to reprocess");
            return 0;
        }

        log.info("Reprocessing {} pending failed events", pendingEvents.size());
        pendingEvents.forEach(f-> reprocessFailedEvent(f.getId()));
        return pendingEvents.size();
    }


    @Transactional
    public void reprocessFailedEvent(UUID failedEventId) {
      FailedEvent failedEvent = failedEventService.findById(failedEventId).orElse(null);
        if (failedEvent == null) {
            log.warn("Failed event not found, skipping: id={}", failedEventId);
            return;
        }

        long attemptCount = failedEventService.countAttempts(failedEvent.getEventId());
        if (attemptCount >= MAX_AUTO_RETRY_ATTEMPTS) {
            log.warn("Event {} has reached max auto-retry ({}), skipping - manuel review required", failedEvent.getEventId(), MAX_AUTO_RETRY_ATTEMPTS);
            return;
        }

        try {
            OrderEvent orderEvent = objectMapper.readValue(failedEvent.getPayload(), OrderEvent.class);
            kafkaTemplate.send("order-events", String.valueOf(failedEvent.getOrderId()), orderEvent);
            failedEventService.markAsRetried(failedEvent.getId());
            log.info("Failed event resubmitted: eventId={}, orderId={}", orderEvent.getEventId(), orderEvent.getOrderId());
        } catch (JsonProcessingException ex) {
            log.error("Failed to parse failed event payload, reprocess aborted: id={}", failedEvent.getId(), ex);
        }
    }
}
