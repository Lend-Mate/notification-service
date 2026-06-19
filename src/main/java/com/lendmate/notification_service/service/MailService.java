package com.lendmate.notification_service.service;

import jakarta.mail.MessagingException;

public interface MailService {
    void sendPlainText(String to, String subject, String body);
    void sendHtml(String to, String subject, String htmlBody);
    void sendOrderConfirmation(String to, Long orderId);
}
