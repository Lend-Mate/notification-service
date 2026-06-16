package com.lendmate.notification_service.dto.request;

public record MailRequest(String to, String subject, String body) {
}