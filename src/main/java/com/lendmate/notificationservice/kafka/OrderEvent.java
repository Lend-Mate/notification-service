package com.lendmate.notificationservice.kafka;

import com.lendmate.notificationservice.dto.response.OrderItemRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderEvent {
    private Long orderId;
    private String status;
    private Long userId;
    private String orderNumber;
    private List<OrderItemRequest> items;
}
