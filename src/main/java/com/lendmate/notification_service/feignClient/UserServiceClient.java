package com.lendmate.notification_service.feignClient;

import com.lendmate.notification_service.dto.response.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", url = "${user-service.url}")
public interface UserServiceClient {
    @GetMapping("/user/internal/{id}")
    UserResponse getUserById(@PathVariable Long id);

    @GetMapping("/user/internal/{id}/email")
    String getEmailByUserId(@PathVariable Long id);
}
