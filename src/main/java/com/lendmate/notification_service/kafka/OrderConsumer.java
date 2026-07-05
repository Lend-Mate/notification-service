package com.lendmate.notification_service.kafka;

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
                    "https://t4.ftcdn.net/jpg/06/71/92/37/360_F_671923740_x0zOL3OIuUAnSF6sr7PuznCI5bQFKhI0.jpg" :
                    "https://lend-mate-bucket.s3.amazonaws.com/" + productDetail.getImages().getFirst().getImageUrl();

            InfoOwnerDto infoOwnerDto = new InfoOwnerDto(
                    owner.getFirstName() + " " + owner.getLastName(),
                    owner.getEmail(),
                    productDetail.getProductName(),
                    productDetail.getDescription(),
                    productDetail.getPrice(),
                    imageUrl,
                    event.getOrderNumber(),
                    startDate,
                    endDate,
                    totalEarning
            );

            mailService.sendInfoToProductOwners(infoOwnerDto);
        }


        log.info("Order event received: orderId={}, status={}, userId={}, orderNumber={}", event.getOrderId(), event.getStatus(), event.getUserId(), event.getOrderNumber());
        mailService.sendOrderConfirmation(user.getEmail(), event.getOrderNumber());
    }
}
