package com.lendmate.notification_service.kafka;

import com.lendmate.notification_service.feignClient.UserServiceClient;
import com.lendmate.notification_service.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderConsumer {
    private final MailService mailService;
    private final UserServiceClient userServiceClient;
    @KafkaListener(topics = "order-topic", groupId = "notification-service")
    public void handleOrderEvent(OrderEvent event){
        String email = userServiceClient.getEmailByUserId(event.getUserId());
        String ownerEmail = userServiceClient.getEmailByUserId(event.getUserId());

        log.info("Order event received: orderId={}, status={}, userId={}, orderNumber={}", event.getOrderId(), event.getStatus(), event.getUserId(), event.getOrderNumber());
        mailService.sendOrderConfirmation(email, event.getOrderNumber());
    }
}
