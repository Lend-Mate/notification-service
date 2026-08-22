package com.lendmate.notificationservice.mapper;

import com.lendmate.notificationservice.dto.request.NotificationRequest;
import com.lendmate.notificationservice.dto.response.NotificationResponse;
import com.lendmate.notificationservice.model.Notification;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    Notification toEntity(NotificationRequest requestDto);

    NotificationResponse toResponseDto(Notification notification);
}