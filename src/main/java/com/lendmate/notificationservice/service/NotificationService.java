package com.lendmate.notificationservice.service;

import com.lendmate.notificationservice.dto.request.NotificationRequest;
import com.lendmate.notificationservice.dto.response.NotificationResponse;

import java.util.List;

public interface NotificationService {
    NotificationResponse getNotificationById(Long id);
    List<NotificationResponse> getAllNotifications();
    void saveNotification(NotificationRequest notificationRequest);
    void deleteNotification(Long productId);
}
