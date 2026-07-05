package com.lendmate.notification_service.service;

import com.lendmate.notification_service.dto.InfoOwnerDto;

public interface MailService {
    void sendPlainText(String to, String subject, String body);
    void sendHtml(String to, String subject, String htmlBody);
    void sendOrderConfirmation(String to, String orderNumber);
    void sendInfoToProductOwners(InfoOwnerDto infoOwnerDto);
}
