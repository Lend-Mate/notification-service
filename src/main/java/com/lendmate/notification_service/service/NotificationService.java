package com.lendmate.notification_service.service;

import com.lendmate.notification_service.dto.request.NotificationRequest;
import com.lendmate.notification_service.dto.response.NotificationResponse;
import com.lendmate.notification_service.model.Notification;

import java.util.List;

public interface NotificationService {
    NotificationResponse getNotificationById(Long id);
    List<NotificationResponse> getAllNotifications();
    void saveNotification(NotificationRequest notificationRequest);
    void deleteNotification(Long productId);
}
