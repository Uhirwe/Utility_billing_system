package com.utilitybilling.mapper;

import com.utilitybilling.dto.notification.NotificationResponse;
import com.utilitybilling.entity.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .customerId(notification.getCustomer().getId())
                .customerName(notification.getCustomer().getFullNames())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .notificationDate(notification.getNotificationDate())
                .readStatus(notification.getReadStatus())
                .build();
    }
}
