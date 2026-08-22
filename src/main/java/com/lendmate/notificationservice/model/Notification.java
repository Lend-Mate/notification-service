package com.lendmate.notificationservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int userId;

    private String type;

    private String channel;

    private String title;

    private String message;

    private String status;

    private LocalDateTime readAt;

    private LocalDateTime sentAt;

    @CreationTimestamp
    private LocalDateTime createdAt;
}