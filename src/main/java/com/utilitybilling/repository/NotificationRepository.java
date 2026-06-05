package com.utilitybilling.repository;

import com.utilitybilling.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByCustomerId(Long customerId, Pageable pageable);
    boolean existsByCustomerIdAndTitleAndMessage(Long customerId, String title, String message);
}
