package com.lendmate.notificationservice.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderEventDltListener {

    @KafkaListener(topics = "order-events.DLT", groupId = "notification-service-dlt", containerFactory = "dltListenerContainerFactory")
    public void handleDtl(@Payload OrderEvent event,
                          @Header (value = KafkaHeaders.DLT_EXCEPTION_MESSAGE, required = false) String exceptionMessage,
                          @Header(value = KafkaHeaders.DLT_ORIGINAL_TOPIC, required = false) String originalTopic
        ) {
        log.error("DLT events: orderId={}, eventId={}, originalTopic={}, exception={}", event.getOrderId(),
                event.getEventId(), originalTopic, exceptionMessage);
    }
}
