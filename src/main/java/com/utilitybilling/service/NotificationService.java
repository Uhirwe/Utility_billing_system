package com.utilitybilling.service;

import com.utilitybilling.dto.notification.NotificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {
    Page<NotificationResponse> getNotifications(Long customerId, Pageable pageable);
    void markAsRead(Long id, String requesterEmail);
}
