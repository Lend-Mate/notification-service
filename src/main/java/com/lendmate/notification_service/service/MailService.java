package com.lendmate.notification_service.service;


import java.util.concurrent.CompletableFuture;

public interface MailService {
    void sendPlainText(String to, String subject, String body);
    CompletableFuture<Boolean> sendHtml(String to, String subject, String htmlBody);
}
