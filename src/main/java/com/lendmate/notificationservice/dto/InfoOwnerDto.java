package com.lendmate.notificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class InfoOwnerDto {
    private String title;
    private String ownerName;
    private String ownerEmail;
    private String productName;
    private String productDescription;
    private BigDecimal productPrice;
    private String productImage;
    private String orderNumber;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private BigDecimal totalEarning;
    private String html;
}
