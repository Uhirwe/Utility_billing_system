package com.utilitybilling.service.impl;

import com.utilitybilling.dto.notification.NotificationResponse;
import com.utilitybilling.entity.Customer;
import com.utilitybilling.entity.Notification;
import com.utilitybilling.exception.BusinessRuleException;
import com.utilitybilling.exception.ResourceNotFoundException;
import com.utilitybilling.mapper.NotificationMapper;
import com.utilitybilling.repository.CustomerRepository;
import com.utilitybilling.repository.NotificationRepository;
import com.utilitybilling.service.CustomerAccessService;
import com.utilitybilling.service.NotificationService;
import com.utilitybilling.util.ValidationConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final CustomerRepository customerRepository;
    private final NotificationMapper notificationMapper;
    private final CustomerAccessService customerAccessService;

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotifications(Long customerId, Pageable pageable) {
        return notificationRepository.findByCustomerId(customerId, pageable)
                .map(notificationMapper::toResponse);
    }

    @Override
    @Transactional
    public void markAsRead(Long id, String requesterEmail) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));
        customerAccessService.assertStaffOrOwnCustomer(requesterEmail, notification.getCustomer().getId());
        notification.setReadStatus(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void sendCustomerNotification(Long customerId, String title, String message) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        if (customer.getEmail() == null || !customer.getEmail().matches(ValidationConstants.EMAIL_PATTERN)) {
            throw new BusinessRuleException("Customer must have a valid email before sending notifications");
        }

        if (notificationRepository.existsByCustomerIdAndTitleAndMessage(customerId, title, message)) {
            return;
        }

        notificationRepository.save(Notification.builder()
                .customer(customer)
                .title(title)
                .message(message)
                .readStatus(false)
                .build());
    }
}
