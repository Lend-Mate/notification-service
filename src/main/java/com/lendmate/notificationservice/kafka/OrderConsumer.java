package com.lendmate.notificationservice.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lendmate.notificationservice.dto.request.NotificationRequest;
import com.lendmate.notificationservice.service.FailedEventService;
import com.lendmate.notificationservice.service.NotificationService;
import com.lendmate.notificationservice.service.ProcessedEventService;
import com.lendmate.notificationservice.utility.Constants;
import com.lendmate.notificationservice.dto.response.ProductResponse;
import com.lendmate.notificationservice.dto.response.UserResponse;
import com.lendmate.notificationservice.feignClient.ProductServiceClient;
import com.lendmate.notificationservice.feignClient.UserServiceClient;
import com.lendmate.notificationservice.service.MailService;
import feign.RetryableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.math.BigDecimal;
import java.net.ConnectException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderConsumer {
    private final MailService mailService;
    private final UserServiceClient userServiceClient;
    private final ProductServiceClient productServiceClient;
    private final NotificationService notificationService;
    private final SpringTemplateEngine templateEngine;
    private final ProcessedEventService processedEventService;
    private final ObjectMapper objectMapper;
    private final FailedEventService failedEventService;

    @RetryableTopic(
            attempts = "4",
            backoff = @Backoff(delay = 2000, multiplier = 2, maxDelay = 5000),
            autoCreateTopics = "true",
            numPartitions = "1",
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
            dltTopicSuffix = ".DLT",
            include = {
                    ConnectException.class,
                    ResourceAccessException.class,
                    TimeoutException.class,
                    RetryableException.class
            })
    @KafkaListener(topics = "order-events", groupId = "notification-service")
    public void handleOrderEvent(OrderEvent event) throws ExecutionException, InterruptedException {

        if (processedEventService.isProcessed(event.getEventId())) {
            log.info("Event already processed, skip: eventId={}, orderId={}", event.getEventId(), event.getOrderId());
            return;
        }

        UserResponse user = userServiceClient.getUserById(event.getUserId());

        for (int i = 0; i < event.getItems().size(); i++) {
            Long productId = event.getItems().get(i).getProductId();
            LocalDateTime startDate = event.getItems().get(i).getStartDate();
            LocalDateTime endDate = event.getItems().get(i).getEndDate();


            ProductResponse productDetail = productServiceClient.getProductDetail(productId);
            UserResponse owner = userServiceClient.getUserById(productDetail.getOwnerId());

            // Calculate total earning by multiplying price by day difference between start and end date
            long daysBetween = Duration.between(startDate, endDate).toDays();
            BigDecimal totalEarning = productDetail.getPrice().multiply(BigDecimal.valueOf(daysBetween));

            String imageUrl = productDetail.getImages().isEmpty() ?
                    Constants.IMAGE_DEFAULT_URL :
                    Constants.S3_BUCKET_URL + productDetail.getImages().getFirst().getImageUrl();

            String title = Constants.ORDER_TITLE_FOR_OWNER;

            String htmlContent = generateHtmlContent(owner, productDetail, imageUrl, event, startDate, endDate, totalEarning, "info-to-owners");
            CompletableFuture<Boolean> isSuccessFuture = mailService.sendHtml(owner.getEmail(), title, htmlContent);

            boolean isSuccess = isSuccessFuture.get();
            NotificationRequest notificationReq = new NotificationRequest(
                    owner.getId(),
                    "INFORM_TO_OWNER",
                    "EMAIL",
                    title,
                    htmlContent,

                    //TODO: bildirim statusleri oluşturulacak enum şeklinde 'FAILED, PROCESSED' vs.
                    isSuccess ? "COMPLETED" : "FAILED"
            );
            notificationService.saveNotification(notificationReq);

        }

        String title = Constants.ORDER_TITLE_FOR_CUSTOMER;

        String htmlContent = generateHtmlContent(null, null, null, event, null, null, null, "order-confirmation");

        log.info("Order event received: orderId={}, status={}, userId={}, orderNumber={}", event.getOrderId(), event.getStatus(), event.getUserId(), event.getOrderNumber());
        CompletableFuture<Boolean> isSuccessFuture = mailService.sendHtml(user.getEmail(), title, htmlContent);
        boolean isSuccess = isSuccessFuture.get();

        NotificationRequest notificationReq = new NotificationRequest(
                user.getId(),
                "INFORM_TO_CUSTOMER",
                "EMAIL",
                title,
                htmlContent,

                //TODO: bildirim statusleri oluşturulacak enum şeklinde 'FAILED, PROCESSED' vs.
                isSuccess ? "COMPLETED" : "FAILED"
        );
        notificationService.saveNotification(notificationReq);
        processedEventService.markAsProcessed(event.getEventId());
        // TODO: ileride Dead Letter Topic (DLT) stratejisi eklenip mesaj kaybı önlenebilir
    }

    @DltHandler
    public void handleDlt(@Payload OrderEvent event,
                          @Header(KafkaHeaders.EXCEPTION_MESSAGE) String exceptionMessage,
                          @Header(KafkaHeaders.ORIGINAL_TOPIC) String originalTopic) throws JsonProcessingException {
        log.error("DLT events: orderId={}, eventId={}, orijinalTopic={}, hata={}", event.getOrderId(), event.getEventId(), originalTopic, exceptionMessage);

        String payloadJson = objectMapper.writeValueAsString(event);
        failedEventService.recordFailure(event.getEventId(), event.getOrderId(), originalTopic, payloadJson, exceptionMessage);

    }

    private String generateHtmlContent(
            UserResponse owner,
            ProductResponse productDetail,
            String imageUrl,
            OrderEvent event,
            LocalDateTime startDate,
            LocalDateTime endDate,
            BigDecimal totalEarning,
            String templateName
    ){
        Context context = new Context();

        if(owner != null) {
            context.setVariable("ownerName", owner.getFirstName() + " " + owner.getLastName());
            context.setVariable("ownerEmail", owner.getEmail());
        }

        if (productDetail != null) {
            context.setVariable("productName", productDetail.getProductName());
            context.setVariable("productDescription", productDetail.getDescription());
            context.setVariable("productPrice", productDetail.getPrice());
            context.setVariable("productImageUrl", imageUrl);
        }

        if (event != null) {
            context.setVariable("orderNumber", event.getOrderNumber());
        }
        if (startDate != null) {
            context.setVariable("startDate", startDate);
        }
        if (endDate != null) {
            context.setVariable("endDate", endDate);
        }
        if (totalEarning != null) {
            context.setVariable("totalEarning", totalEarning);
        }
        return templateEngine.process(templateName, context);
    }
}
