package com.lendmate.notificationservice.service.impl;

import com.lendmate.notificationservice.model.ProcessedEvent;
import com.lendmate.notificationservice.repository.ProcessedEventRepository;
import com.lendmate.notificationservice.service.ProcessedEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProcessedEventServiceImpl implements ProcessedEventService {
    private final ProcessedEventRepository repository;

    @Override
    public boolean isProcessed(UUID eventId) {
        return repository.existsByEventId(eventId);
    }

    @Override
    @Transactional
    public void markAsProcessed(UUID eventId) {
        try {
            repository.save(new ProcessedEvent(eventId, Instant.now()));
        }catch (DataIntegrityViolationException ex){
            log.warn("Event already marked: eventId={}", eventId);
        }

    }
}
