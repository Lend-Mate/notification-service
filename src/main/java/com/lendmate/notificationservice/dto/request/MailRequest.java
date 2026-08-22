package com.lendmate.notificationservice.dto.request;

public record MailRequest(String to, String subject, String body) {
}