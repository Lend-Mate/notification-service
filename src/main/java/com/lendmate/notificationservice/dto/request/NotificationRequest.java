package com.lendmate.notificationservice.dto.request;

public record NotificationRequest(Long userId, String type, String channel, String title, String message,
                                  String status) {
}