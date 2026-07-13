package com.lendmate.notification_service.kafka;

import com.lendmate.notification_service.dto.request.NotificationRequest;
import com.lendmate.notification_service.service.NotificationService;
import com.lendmate.notification_service.utility.Constants;
import com.lendmate.notification_service.dto.InfoOwnerDto;
import com.lendmate.notification_service.dto.response.ProductResponse;
import com.lendmate.notification_service.dto.response.UserResponse;
import com.lendmate.notification_service.feignClient.ProductServiceClient;
import com.lendmate.notification_service.feignClient.UserServiceClient;
import com.lendmate.notification_service.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderConsumer {
    private final MailService mailService;
    private final UserServiceClient userServiceClient;
    private final ProductServiceClient productServiceClient;
    private final NotificationService notificationService;
    private final SpringTemplateEngine templateEngine;

    @KafkaListener(topics = "order-topic", groupId = "notification-service")
    public void handleOrderEvent(OrderEvent event){
        UserResponse user = userServiceClient.getUserById(event.getUserId());

        for(int i = 0; i < event.getItems().size(); i++){
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
            mailService.sendHtml(owner.getEmail(), title, htmlContent);

            NotificationRequest notificationReq = new NotificationRequest(
                    owner.getId(),
                    "INFORM_TO_OWNER",
                    "EMAIL",
                    title,
                    htmlContent,

                    //TODO: bildirim statusleri oluşturulacak enum şeklinde 'FAILED, PROCESSED' vs.
                    "COMPLETED"
            );
            notificationService.saveNotification(notificationReq);
        }

        String title = Constants.ORDER_TITLE_FOR_CUSTOMER;

        String htmlContent = generateHtmlContent(null, null, null, event, null, null, null, "order-confirmation");

        log.info("Order event received: orderId={}, status={}, userId={}, orderNumber={}", event.getOrderId(), event.getStatus(), event.getUserId(), event.getOrderNumber());
        mailService.sendHtml(user.getEmail(), title, htmlContent);

        NotificationRequest notificationReq = new NotificationRequest(
                 user.getId(),
                "INFORM_TO_CUSTOMER",
                "EMAIL",
                title,
                htmlContent,

                //TODO: bildirim statusleri oluşturulacak enum şeklinde 'FAILED, PROCESSED' vs.
                "COMPLETED"
        );
        notificationService.saveNotification(notificationReq);
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
