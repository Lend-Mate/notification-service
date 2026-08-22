package com.lendmate.notificationservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Long ownerId;
    private String productName;
    private String description;
    private BigDecimal price;
    private List<ProductImageResponse> images;
}
