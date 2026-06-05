package com.utilitybilling.dto.notification;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {
    private Long id;
    private Long customerId;
    private String customerName;
    private String title;
    private String message;
    private LocalDateTime notificationDate;
    private Boolean readStatus;
}
