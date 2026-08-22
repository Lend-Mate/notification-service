package com.lendmate.notificationservice.feignClient;

import com.lendmate.notificationservice.dto.response.ProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service")
public interface ProductServiceClient {
    @GetMapping("/products/{id}")
    ProductResponse getProductDetail(@PathVariable Long id);
}
