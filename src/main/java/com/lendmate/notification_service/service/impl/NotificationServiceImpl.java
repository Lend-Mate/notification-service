package com.lendmate.notification_service.service.impl;

import com.lendmate.notification_service.dto.request.NotificationRequest;
import com.lendmate.notification_service.dto.response.NotificationResponse;
import com.lendmate.notification_service.mapper.NotificationMapper;
import com.lendmate.notification_service.model.Notification;
import com.lendmate.notification_service.repository.NotificationRepository;
import com.lendmate.notification_service.service.NotificationService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationMapper mapper;

    public NotificationServiceImpl(NotificationRepository notificationRepository, NotificationMapper mapper) {
        this.notificationRepository = notificationRepository;
        this.mapper = mapper;
    }


    @Override
    public NotificationResponse getNotificationById(Long id) {
        Notification n = notificationRepository.getById(id);
        return mapper.toResponseDto(n);
    }

    @Override
    public List<NotificationResponse> getAllNotifications() {
        List<Notification> nList = notificationRepository.findAll();
        return nList.stream().map(mapper::toResponseDto).collect(Collectors.toList());
    }

    @Override
    public void saveNotification(NotificationRequest notificationRequest) {
        notificationRepository.save(mapper.toEntity(notificationRequest));
    }

    @Override
    public void deleteNotification(Long id) {
        notificationRepository.deleteById(id);
    }
}
