package com.lendmate.notificationservice.repository;

import com.lendmate.notificationservice.model.Enum.FailedEventStatus;
import com.lendmate.notificationservice.model.FailedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FailedEventRepository extends JpaRepository<FailedEvent, UUID> {
    List<FailedEvent> findByStatus(FailedEventStatus status);
    long countByEventId(UUID eventId);
}
