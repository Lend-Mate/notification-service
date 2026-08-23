package com.lendmate.notificationservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private Long id;

    private int userId;

    private String type;

    private String channel;

    private String title;

    private String message;

    private String status;

    private LocalDateTime readAt;

    private LocalDateTime sentAt;

    private LocalDateTime createdAt;
}