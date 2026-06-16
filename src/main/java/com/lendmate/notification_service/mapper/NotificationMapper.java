package com.lendmate.notification_service.mapper;

import com.lendmate.notification_service.dto.request.NotificationRequest;
import com.lendmate.notification_service.dto.response.NotificationResponse;
import com.lendmate.notification_service.model.Notification;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    Notification toEntity(NotificationRequest requestDto);

    NotificationResponse toResponseDto(Notification notification);
}