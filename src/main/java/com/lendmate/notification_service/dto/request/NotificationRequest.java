package com.lendmate.notification_service.dto.request;

public record NotificationRequest(int userId, String type, String channel, String title, String message,
                                  String status) {
}